package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.network.SyncSotoExteriorS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Near-live SOTO snapshot sync.
 * <p>
 * Terrain dirty (block edits / shell animation) rebuilds the full sample.
 * Entity occupancy keeps exterior chunks ticketed and mob AI alive every tick, but only
 * refreshes entity samples on a slower cadence without re-flooding terrain.
 */
public final class SotoExteriorSyncService {
    private static final int FLUSH_INTERVAL_TICKS = 3;
    /** How often entity presence triggers an entity-only snapshot refresh. */
    private static final int ENTITY_REFRESH_INTERVAL_TICKS = 20;

    private static final Set<UUID> DIRTY = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ENTITY_REFRESH = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ENTITY_ACTIVE = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> PLAYER_VIEW_DISTANCE = new ConcurrentHashMap<>();
    private static final Map<SnapshotCacheKey, SotoExteriorSnapshot> SNAPSHOT_CACHE = new ConcurrentHashMap<>();
    private static final Map<SnapshotCacheKey, TerrainCache> TERRAIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REVISIONS = new ConcurrentHashMap<>();
    private static int tickCounter;

    private record SnapshotCacheKey(UUID tardisId, int radiusChunks) {
    }

    private record TerrainCache(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            Set<BlockPos> floodedRel
    ) {
    }

    private SotoExteriorSyncService() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient()) {
                markDirtyAt(world.getRegistryKey(), pos);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) {
                markDirtyAt(world.getRegistryKey(), hitResult.getBlockPos());
                markDirtyAt(world.getRegistryKey(), hitResult.getBlockPos().offset(hitResult.getSide()));
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(SotoExteriorSyncService::onEndTick);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            DIRTY.clear();
            ENTITY_REFRESH.clear();
            ENTITY_ACTIVE.clear();
            PLAYER_VIEW_DISTANCE.clear();
            SNAPSHOT_CACHE.clear();
            TERRAIN_CACHE.clear();
            REVISIONS.clear();
            SotoExteriorIndex.clear();
            tickCounter = 0;
        });
    }

    public static void markDirty(UUID tardisId) {
        if (tardisId != null) {
            DIRTY.add(tardisId);
            clearCacheFor(tardisId);
        }
    }

    public static void markDirtyAt(RegistryKey<World> worldKey, BlockPos worldPos) {
        UUID tardisId = SotoExteriorIndex.resolve(worldKey, worldPos);
        if (tardisId != null) {
            markDirty(tardisId);
        }
    }

    public static void rememberPlayerViewDistance(ServerPlayerEntity player, int viewDistanceChunks) {
        if (player == null) {
            return;
        }
        PLAYER_VIEW_DISTANCE.put(
                player.getUuid(),
                SotoExteriorSampler.clampRadiusChunks(viewDistanceChunks)
        );
    }

    public static int viewDistanceFor(ServerPlayerEntity player) {
        if (player == null) {
            return SotoExteriorSampler.DEFAULT_RADIUS_CHUNKS;
        }
        return PLAYER_VIEW_DISTANCE.getOrDefault(
                player.getUuid(),
                SotoExteriorSampler.DEFAULT_RADIUS_CHUNKS
        );
    }

    /**
     * Builds (or returns cached) snapshot and sends it to {@code player}.
     * Skips send when the TARDIS has no exterior location.
     */
    public static boolean sendToPlayer(ServerPlayerEntity player, UUID tardisId) {
        return sendToPlayer(player, tardisId, viewDistanceFor(player));
    }

    public static boolean sendToPlayer(ServerPlayerEntity player, UUID tardisId, int viewDistanceChunks) {
        MinecraftServer server = player.getServer();
        if (server == null || tardisId == null) {
            return false;
        }
        int radiusChunks = SotoExteriorSampler.clampRadiusChunks(viewDistanceChunks);
        rememberPlayerViewDistance(player, radiusChunks);
        SotoExteriorSnapshot snapshot = buildFullSnapshot(server, tardisId, radiusChunks);
        if (snapshot == null) {
            return false;
        }
        ServerPlayNetworking.send(player, SyncSotoExteriorS2CPayload.fromSnapshot(snapshot));
        return true;
    }

    private static void onEndTick(MinecraftServer server) {
        tickCounter++;
        maintainExteriorKeepAlive(server);
        markShellAnimatingDirty(server);

        if (tickCounter % ENTITY_REFRESH_INTERVAL_TICKS == 0 && !ENTITY_REFRESH.isEmpty()) {
            Set<UUID> toRefresh = Set.copyOf(ENTITY_REFRESH);
            ENTITY_REFRESH.removeAll(toRefresh);
            for (UUID tardisId : toRefresh) {
                pushEntityRefreshToInteriorTrackers(server, tardisId);
            }
        }

        if (DIRTY.isEmpty()) {
            return;
        }
        if (tickCounter % FLUSH_INTERVAL_TICKS != 0) {
            return;
        }
        Set<UUID> toFlush = Set.copyOf(DIRTY);
        DIRTY.removeAll(toFlush);
        for (UUID tardisId : toFlush) {
            pushToInteriorTrackers(server, tardisId);
        }
    }

    /**
     * While interior doors are tracked by players: hold exterior chunk tickets (wide RD),
     * reset mob despawn, and schedule slow entity-only refreshes. Never force-loads chunks
     * or marks full terrain dirty.
     */
    private static void maintainExteriorKeepAlive(MinecraftServer server) {
        ServerWorld interiorWorld = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        for (UUID tardisId : SotoExteriorIndex.registeredIds()) {
            RegistryKey<World> worldKey = SotoExteriorIndex.getWorldKey(tardisId);
            BlockPos exteriorPos = SotoExteriorIndex.getExteriorPos(tardisId);
            if (worldKey == null || exteriorPos == null) {
                continue;
            }
            ServerWorld exteriorWorld = server.getWorld(worldKey);
            if (exteriorWorld == null) {
                continue;
            }

            int radiusChunks = keepAliveRadiusChunks(interiorWorld, tardisId);
            boolean viewersPresent = interiorWorld != null && hasInteriorTrackers(interiorWorld, tardisId);

            if (viewersPresent) {
                SotoExteriorSampler.addFootprintTickets(exteriorWorld, exteriorPos, radiusChunks);
            }

            boolean hasEntities = SotoExteriorSampler.hasEntities(exteriorWorld, exteriorPos, radiusChunks);
            if (hasEntities) {
                ENTITY_ACTIVE.add(tardisId);
                SotoExteriorSampler.keepMobAiActive(exteriorWorld, exteriorPos, radiusChunks);
                if (viewersPresent) {
                    ENTITY_REFRESH.add(tardisId);
                }
            } else if (ENTITY_ACTIVE.remove(tardisId) && viewersPresent) {
                // One more entity refresh so clients clear departed mobs.
                ENTITY_REFRESH.add(tardisId);
            }
        }
    }

    private static int keepAliveRadiusChunks(ServerWorld interiorWorld, UUID tardisId) {
        if (interiorWorld == null) {
            return SotoExteriorSampler.DEFAULT_RADIUS_CHUNKS;
        }
        BlockPos doorOrigin = TardisPlotAllocator.plotOrigin(tardisId)
                .add(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        int max = SotoExteriorSampler.MIN_RADIUS_CHUNKS;
        boolean any = false;
        for (ServerPlayerEntity player : PlayerLookup.tracking(interiorWorld, doorOrigin)) {
            any = true;
            max = Math.max(max, viewDistanceFor(player));
        }
        return any ? max : SotoExteriorSampler.DEFAULT_RADIUS_CHUNKS;
    }

    private static boolean hasInteriorTrackers(ServerWorld interiorWorld, UUID tardisId) {
        BlockPos doorOrigin = TardisPlotAllocator.plotOrigin(tardisId)
                .add(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        return !PlayerLookup.tracking(interiorWorld, doorOrigin).isEmpty();
    }

    /** Keep shell door swing in sync while animating. */
    private static void markShellAnimatingDirty(MinecraftServer server) {
        for (UUID tardisId : SotoExteriorIndex.registeredIds()) {
            TardisDataModel model = TardisDataLoader.get(tardisId);
            if (model == null || model.doorState == null) {
                continue;
            }
            float swing = model.doorState.doorSwing;
            if (swing > 0.0f && swing < 1.0f) {
                markDirty(tardisId);
            }
        }
    }

    private static SotoExteriorSnapshot buildFullSnapshot(
            MinecraftServer server,
            UUID tardisId,
            int radiusChunks
    ) {
        int clamped = SotoExteriorSampler.clampRadiusChunks(radiusChunks);
        SnapshotCacheKey cacheKey = new SnapshotCacheKey(tardisId, clamped);
        SotoExteriorSnapshot cached = SNAPSHOT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ExteriorContext ctx = resolveExterior(server, tardisId);
        if (ctx == null) {
            return null;
        }

        SotoExteriorSampler.FullSample sampled = SotoExteriorSampler.sampleAll(
                ctx.world(), ctx.exteriorPos(), clamped, ctx.exteriorRotation()
        );
        TERRAIN_CACHE.put(cacheKey, new TerrainCache(
                sampled.blocks(),
                sampled.blockEntities(),
                sampled.floodedRel()
        ));

        SotoExteriorSnapshot snapshot = toSnapshot(
                tardisId,
                clamped,
                sampled.blocks(),
                sampled.blockEntities(),
                sampled.entities(),
                ctx.model()
        );
        SNAPSHOT_CACHE.put(cacheKey, snapshot);
        return snapshot;
    }

    /**
     * Rebuilds a snapshot using cached terrain + fresh entity samples (no flood-fill).
     * Falls back to a full sample when terrain has not been cached yet.
     */
    private static SotoExteriorSnapshot buildEntityRefreshSnapshot(
            MinecraftServer server,
            UUID tardisId,
            int radiusChunks
    ) {
        int clamped = SotoExteriorSampler.clampRadiusChunks(radiusChunks);
        SnapshotCacheKey cacheKey = new SnapshotCacheKey(tardisId, clamped);
        TerrainCache terrain = TERRAIN_CACHE.get(cacheKey);
        if (terrain == null) {
            return buildFullSnapshot(server, tardisId, clamped);
        }

        ExteriorContext ctx = resolveExterior(server, tardisId);
        if (ctx == null) {
            return null;
        }

        List<BotiEntitySample> entities = SotoExteriorSampler.sampleEntitiesOnly(
                ctx.world(), ctx.exteriorPos(), clamped, terrain.floodedRel()
        );
        SotoExteriorSnapshot snapshot = toSnapshot(
                tardisId,
                clamped,
                terrain.blocks(),
                terrain.blockEntities(),
                entities,
                ctx.model()
        );
        SNAPSHOT_CACHE.put(cacheKey, snapshot);
        return snapshot;
    }

    private static SotoExteriorSnapshot toSnapshot(
            UUID tardisId,
            int radiusChunks,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities,
            TardisDataModel model
    ) {
        int revision = REVISIONS.merge(tardisId, 1, Integer::sum);
        TardisDoorState doorState = model.doorState == null ? new TardisDoorState() : model.doorState;
        TardisChameleonVariant variant =
                model.variant == null ? TardisChameleonVariant.TT_CAPSULE : model.variant;
        return SotoExteriorSnapshot.of(
                tardisId,
                revision,
                radiusChunks,
                blocks,
                blockEntities,
                entities,
                variant,
                doorState.doorSwing,
                doorState.isOpen,
                model.exteriorRotation
        );
    }

    private static ExteriorContext resolveExterior(MinecraftServer server, UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return null;
        }
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(model.exteriorDimension));
        ServerWorld exteriorWorld = server.getWorld(worldKey);
        if (exteriorWorld == null) {
            return null;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        SotoExteriorIndex.register(tardisId, worldKey, exteriorPos);
        return new ExteriorContext(exteriorWorld, exteriorPos, model.exteriorRotation, model);
    }

    private record ExteriorContext(
            ServerWorld world,
            BlockPos exteriorPos,
            int exteriorRotation,
            TardisDataModel model
    ) {
    }

    private static void pushToInteriorTrackers(MinecraftServer server, UUID tardisId) {
        for (ServerPlayerEntity player : interiorTrackers(server, tardisId)) {
            SotoExteriorSnapshot snapshot = buildFullSnapshot(server, tardisId, viewDistanceFor(player));
            if (snapshot == null) {
                continue;
            }
            ServerPlayNetworking.send(player, SyncSotoExteriorS2CPayload.fromSnapshot(snapshot));
        }
    }

    private static void pushEntityRefreshToInteriorTrackers(MinecraftServer server, UUID tardisId) {
        for (ServerPlayerEntity player : interiorTrackers(server, tardisId)) {
            SotoExteriorSnapshot snapshot =
                    buildEntityRefreshSnapshot(server, tardisId, viewDistanceFor(player));
            if (snapshot == null) {
                continue;
            }
            ServerPlayNetworking.send(player, SyncSotoExteriorS2CPayload.fromSnapshot(snapshot));
        }
    }

    private static Collection<ServerPlayerEntity> interiorTrackers(MinecraftServer server, UUID tardisId) {
        ServerWorld interiorWorld = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return List.of();
        }
        BlockPos doorOrigin = TardisPlotAllocator.plotOrigin(tardisId)
                .add(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        return PlayerLookup.tracking(interiorWorld, doorOrigin);
    }

    private static void clearCacheFor(UUID tardisId) {
        SNAPSHOT_CACHE.keySet().removeIf(key -> key.tardisId().equals(tardisId));
        TERRAIN_CACHE.keySet().removeIf(key -> key.tardisId().equals(tardisId));
    }
}
