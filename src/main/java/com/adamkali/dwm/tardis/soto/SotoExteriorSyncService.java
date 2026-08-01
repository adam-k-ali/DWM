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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Near-live SOTO snapshot sync: dirty on exterior edits / door-shell changes and while entities
 * occupy the exterior footprint. Pushes to players tracking the interior door origin.
 */
public final class SotoExteriorSyncService {
    private static final int FLUSH_INTERVAL_TICKS = 3;

    private static final Set<UUID> DIRTY = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ENTITY_ACTIVE = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, SotoExteriorSnapshot> LAST_SNAPSHOT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REVISIONS = new ConcurrentHashMap<>();
    private static int tickCounter;

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
            ENTITY_ACTIVE.clear();
            LAST_SNAPSHOT.clear();
            REVISIONS.clear();
            SotoExteriorIndex.clear();
            tickCounter = 0;
        });
    }

    public static void markDirty(UUID tardisId) {
        if (tardisId != null) {
            DIRTY.add(tardisId);
        }
    }

    public static void markDirtyAt(RegistryKey<World> worldKey, BlockPos worldPos) {
        UUID tardisId = SotoExteriorIndex.resolve(worldKey, worldPos);
        if (tardisId != null) {
            DIRTY.add(tardisId);
        }
    }

    public static SotoExteriorSnapshot getLastSnapshot(UUID tardisId) {
        return LAST_SNAPSHOT.get(tardisId);
    }

    /**
     * Builds (or returns cached) snapshot and sends it to {@code player}.
     * Skips send when the TARDIS has no exterior location.
     */
    public static boolean sendToPlayer(ServerPlayerEntity player, UUID tardisId) {
        MinecraftServer server = player.getServer();
        if (server == null || tardisId == null) {
            return false;
        }
        SotoExteriorSnapshot snapshot = buildSnapshot(server, tardisId);
        if (snapshot == null) {
            return false;
        }
        ServerPlayNetworking.send(player, SyncSotoExteriorS2CPayload.fromSnapshot(snapshot));
        return true;
    }

    private static void onEndTick(MinecraftServer server) {
        tickCounter++;
        markEntityOccupiedPlotsDirty(server);
        markShellAnimatingDirty(server);
        if (DIRTY.isEmpty()) {
            return;
        }
        int interval = intersectsEntityActive(DIRTY) ? 1 : FLUSH_INTERVAL_TICKS;
        if (tickCounter % interval != 0) {
            return;
        }
        Set<UUID> toFlush = Set.copyOf(DIRTY);
        DIRTY.removeAll(toFlush);
        for (UUID tardisId : toFlush) {
            SotoExteriorSnapshot snapshot = buildSnapshot(server, tardisId);
            if (snapshot == null) {
                continue;
            }
            pushToInteriorTrackers(server, snapshot);
        }
    }

    private static boolean intersectsEntityActive(Set<UUID> dirty) {
        for (UUID tardisId : dirty) {
            if (ENTITY_ACTIVE.contains(tardisId)) {
                return true;
            }
        }
        return false;
    }

    private static void markEntityOccupiedPlotsDirty(MinecraftServer server) {
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
            boolean hasEntities = SotoExteriorSampler.hasEntities(exteriorWorld, exteriorPos);
            if (hasEntities || ENTITY_ACTIVE.contains(tardisId)) {
                markDirty(tardisId);
                if (hasEntities) {
                    ENTITY_ACTIVE.add(tardisId);
                    SotoExteriorSampler.keepMobAiActive(exteriorWorld, exteriorPos);
                } else {
                    ENTITY_ACTIVE.remove(tardisId);
                }
            }
        }
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

    private static SotoExteriorSnapshot buildSnapshot(MinecraftServer server, UUID tardisId) {
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

        int revision = REVISIONS.merge(tardisId, 1, Integer::sum);
        Map<BlockPos, net.minecraft.block.BlockState> blocks = SotoExteriorSampler.sample(exteriorWorld, exteriorPos);
        Map<BlockPos, net.minecraft.nbt.NbtCompound> blockEntities =
                SotoExteriorSampler.sampleBlockEntities(exteriorWorld, exteriorPos);
        List<BotiEntitySample> entities = SotoExteriorSampler.sampleEntities(exteriorWorld, exteriorPos);

        TardisDoorState doorState = model.doorState == null ? new TardisDoorState() : model.doorState;
        TardisChameleonVariant variant =
                model.variant == null ? TardisChameleonVariant.TT_CAPSULE : model.variant;

        SotoExteriorSnapshot snapshot = SotoExteriorSnapshot.of(
                tardisId,
                revision,
                blocks,
                blockEntities,
                entities,
                variant,
                doorState.doorSwing,
                doorState.isOpen,
                model.exteriorRotation
        );
        LAST_SNAPSHOT.put(tardisId, snapshot);
        return snapshot;
    }

    private static void pushToInteriorTrackers(MinecraftServer server, SotoExteriorSnapshot snapshot) {
        ServerWorld interiorWorld = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return;
        }
        BlockPos doorOrigin = TardisPlotAllocator.plotOrigin(snapshot.tardisId())
                .add(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        SyncSotoExteriorS2CPayload payload = SyncSotoExteriorS2CPayload.fromSnapshot(snapshot);
        for (ServerPlayerEntity player : PlayerLookup.tracking(interiorWorld, doorOrigin)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
