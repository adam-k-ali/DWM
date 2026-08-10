package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.network.SyncSotoExteriorChunkS2CPayload;
import com.adamkali.dwm.network.SyncSotoExteriorEntityRemoveS2CPayload;
import com.adamkali.dwm.network.SyncSotoExteriorEntitySpawnS2CPayload;
import com.adamkali.dwm.network.SyncSotoExteriorEntityUpdateS2CPayload;
import com.adamkali.dwm.network.UnloadSotoExteriorChunkS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 1 ghost exterior stream: tickets a fixed 2-chunk radius and pushes chunk + live entity
 * packets to interior door trackers / explicit C2S subscribers.
 */
public final class SotoGhostSyncService {
    /** ~10 Hz entity pose updates. */
    private static final int ENTITY_UPDATE_INTERVAL_TICKS = 2;

    /** Viewer leave grace before unloading ghost state. */
    private static final int VIEWER_LEAVE_GRACE_TICKS = 40;

    private static final Map<UUID, Set<UUID>> SUBSCRIBERS = new ConcurrentHashMap<>();
    private static final Map<ViewerKey, Set<Long>> SENT_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<ViewerKey, Set<UUID>> TRACKED_ENTITIES = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<Long>> DIRTY_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LEAVE_GRACE = new ConcurrentHashMap<>();
    private static int tickCounter;

    private SotoGhostSyncService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(SotoGhostSyncService::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> clear());
    }

    public static void clear() {
        SUBSCRIBERS.clear();
        SENT_CHUNKS.clear();
        TRACKED_ENTITIES.clear();
        DIRTY_CHUNKS.clear();
        LEAVE_GRACE.clear();
        tickCounter = 0;
    }

    public static boolean subscribe(ServerPlayer player, UUID tardisId) {
        if (player == null || tardisId == null) {
            return false;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        ExteriorContext ctx = resolveExterior(server, tardisId);
        if (ctx == null) {
            return false;
        }
        SUBSCRIBERS.computeIfAbsent(tardisId, id -> ConcurrentHashMap.newKeySet()).add(player.getUUID());
        LEAVE_GRACE.remove(tardisId);
        sendFullState(player, ctx);
        return true;
    }

    public static void markChunkDirtyAt(ResourceKey<Level> worldKey, BlockPos worldPos) {
        if (worldKey == null || worldPos == null) {
            return;
        }
        for (UUID tardisId : SotoExteriorIndex.registeredIds()) {
            ResourceKey<Level> key = SotoExteriorIndex.getWorldKey(tardisId);
            BlockPos exteriorPos = SotoExteriorIndex.getExteriorPos(tardisId);
            if (key == null || exteriorPos == null || !key.equals(worldKey)) {
                continue;
            }
            if (!SotoExteriorSampler.isInsideStreamRadius(worldPos, exteriorPos)) {
                continue;
            }
            long packed = ChunkPos.pack(
                    worldPos.getX() >> 4,
                    worldPos.getZ() >> 4
            );
            DIRTY_CHUNKS.computeIfAbsent(tardisId, id -> ConcurrentHashMap.newKeySet()).add(packed);
        }
    }

    private static void onEndTick(MinecraftServer server) {
        tickCounter++;
        ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return;
        }

        Set<UUID> activeTardises = new HashSet<>();
        Set<UUID> dirtyFlushed = new HashSet<>();
        for (UUID tardisId : SotoExteriorIndex.registeredIds()) {
            Set<ServerPlayer> viewers = collectViewers(server, interiorWorld, tardisId);
            if (viewers.isEmpty()) {
                handleNoViewers(server, tardisId);
                continue;
            }
            activeTardises.add(tardisId);
            LEAVE_GRACE.remove(tardisId);
            ExteriorContext ctx = resolveExterior(server, tardisId);
            if (ctx == null) {
                continue;
            }
            SotoExteriorSampler.addStreamTickets(ctx.world(), ctx.exteriorPos());
            SotoExteriorSampler.keepMobAiActive(ctx.world(), ctx.exteriorPos());

            for (ServerPlayer viewer : viewers) {
                ensureChunks(viewer, ctx);
                flushDirtyChunks(viewer, ctx);
            }
            dirtyFlushed.add(tardisId);
            if (tickCounter % ENTITY_UPDATE_INTERVAL_TICKS == 0) {
                for (ServerPlayer viewer : viewers) {
                    syncEntities(viewer, ctx);
                }
            }
        }
        for (UUID tardisId : dirtyFlushed) {
            DIRTY_CHUNKS.remove(tardisId);
        }

        // Drop subscriber entries for TARDIS no longer registered
        SUBSCRIBERS.keySet().removeIf(id -> !SotoExteriorIndex.isRegistered(id) && !activeTardises.contains(id));
    }

    private static void handleNoViewers(MinecraftServer server, UUID tardisId) {
        int grace = LEAVE_GRACE.merge(tardisId, 1, Integer::sum);
        if (grace < VIEWER_LEAVE_GRACE_TICKS) {
            return;
        }
        unloadAllViewers(server, tardisId);
        SUBSCRIBERS.remove(tardisId);
        DIRTY_CHUNKS.remove(tardisId);
        LEAVE_GRACE.remove(tardisId);
    }

    private static void unloadAllViewers(MinecraftServer server, UUID tardisId) {
        Set<ViewerKey> keys = new HashSet<>();
        for (ViewerKey key : SENT_CHUNKS.keySet()) {
            if (key.tardisId.equals(tardisId)) {
                keys.add(key);
            }
        }
        for (ViewerKey key : TRACKED_ENTITIES.keySet()) {
            if (key.tardisId.equals(tardisId)) {
                keys.add(key);
            }
        }
        for (ViewerKey key : keys) {
            ServerPlayer player = server.getPlayerList().getPlayer(key.playerId);
            if (player != null) {
                Set<Long> chunks = SENT_CHUNKS.getOrDefault(key, Set.of());
                for (long packed : chunks) {
                    ServerPlayNetworking.send(player, new UnloadSotoExteriorChunkS2CPayload(
                            tardisId, ChunkPos.getX(packed), ChunkPos.getZ(packed)
                    ));
                }
                Set<UUID> entities = TRACKED_ENTITIES.getOrDefault(key, Set.of());
                for (UUID entityId : entities) {
                    ServerPlayNetworking.send(player, new SyncSotoExteriorEntityRemoveS2CPayload(tardisId, entityId));
                }
            }
            SENT_CHUNKS.remove(key);
            TRACKED_ENTITIES.remove(key);
        }
    }

    private static Set<ServerPlayer> collectViewers(
            MinecraftServer server,
            ServerLevel interiorWorld,
            UUID tardisId
    ) {
        Set<ServerPlayer> viewers = ConcurrentHashMap.newKeySet();
        BlockPos doorOrigin = TardisPlotAllocator.plotOrigin(tardisId)
                .offset(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        viewers.addAll(PlayerLookup.tracking(interiorWorld, doorOrigin));
        Set<UUID> subscribers = SUBSCRIBERS.get(tardisId);
        if (subscribers != null) {
            for (UUID playerId : subscribers) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player != null) {
                    viewers.add(player);
                }
            }
        }
        return viewers;
    }

    private static void sendFullState(ServerPlayer player, ExteriorContext ctx) {
        int[] bounds = SotoExteriorSampler.streamChunkBounds(ctx.exteriorPos());
        ViewerKey key = new ViewerKey(ctx.tardisId(), player.getUUID());
        Set<Long> sent = SENT_CHUNKS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                SotoExteriorSampler.StreamChunkSample sample =
                        SotoExteriorSampler.sampleStreamChunk(ctx.world(), ctx.exteriorPos(), cx, cz);
                ServerPlayNetworking.send(
                        player,
                        SyncSotoExteriorChunkS2CPayload.fromSample(ctx.tardisId(), ctx.footprintOrigin(), sample)
                );
                sent.add(ChunkPos.pack(cx, cz));
            }
        }
        TRACKED_ENTITIES.put(key, ConcurrentHashMap.newKeySet());
        syncEntities(player, ctx);
    }

    private static void ensureChunks(ServerPlayer player, ExteriorContext ctx) {
        ViewerKey key = new ViewerKey(ctx.tardisId(), player.getUUID());
        Set<Long> sent = SENT_CHUNKS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        int[] bounds = SotoExteriorSampler.streamChunkBounds(ctx.exteriorPos());
        Set<Long> desired = new HashSet<>();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                desired.add(ChunkPos.pack(cx, cz));
            }
        }
        for (long packed : Set.copyOf(sent)) {
            if (!desired.contains(packed)) {
                sent.remove(packed);
                ServerPlayNetworking.send(player, new UnloadSotoExteriorChunkS2CPayload(
                        ctx.tardisId(), ChunkPos.getX(packed), ChunkPos.getZ(packed)
                ));
            }
        }
        for (long packed : desired) {
            if (sent.contains(packed)) {
                continue;
            }
            int cx = ChunkPos.getX(packed);
            int cz = ChunkPos.getZ(packed);
            SotoExteriorSampler.StreamChunkSample sample =
                    SotoExteriorSampler.sampleStreamChunk(ctx.world(), ctx.exteriorPos(), cx, cz);
            ServerPlayNetworking.send(
                    player,
                    SyncSotoExteriorChunkS2CPayload.fromSample(ctx.tardisId(), ctx.footprintOrigin(), sample)
            );
            sent.add(packed);
        }
    }

    private static void flushDirtyChunks(ServerPlayer player, ExteriorContext ctx) {
        Set<Long> dirty = DIRTY_CHUNKS.get(ctx.tardisId());
        if (dirty == null || dirty.isEmpty()) {
            return;
        }
        ViewerKey key = new ViewerKey(ctx.tardisId(), player.getUUID());
        Set<Long> sent = SENT_CHUNKS.get(key);
        if (sent == null) {
            return;
        }
        for (long packed : Set.copyOf(dirty)) {
            if (!sent.contains(packed)) {
                continue;
            }
            int cx = ChunkPos.getX(packed);
            int cz = ChunkPos.getZ(packed);
            SotoExteriorSampler.StreamChunkSample sample =
                    SotoExteriorSampler.sampleStreamChunk(ctx.world(), ctx.exteriorPos(), cx, cz);
            ServerPlayNetworking.send(
                    player,
                    SyncSotoExteriorChunkS2CPayload.fromSample(ctx.tardisId(), ctx.footprintOrigin(), sample)
            );
        }
    }

    private static void syncEntities(ServerPlayer player, ExteriorContext ctx) {
        ViewerKey key = new ViewerKey(ctx.tardisId(), player.getUUID());
        Set<UUID> tracked = TRACKED_ENTITIES.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        List<Entity> live = SotoExteriorSampler.collectStreamEntities(ctx.world(), ctx.exteriorPos());
        Set<UUID> liveIds = new HashSet<>();
        for (Entity entity : live) {
            if (BotiInteriorSampler.captureEntityNbt(entity) == null && !(entity instanceof net.minecraft.world.entity.player.Player)) {
                continue;
            }
            liveIds.add(entity.getUUID());
            if (tracked.contains(entity.getUUID())) {
                ServerPlayNetworking.send(
                        player,
                        SyncSotoExteriorEntityUpdateS2CPayload.fromEntity(ctx.tardisId(), entity, ctx.footprintOrigin())
                );
            } else {
                SyncSotoExteriorEntitySpawnS2CPayload spawn =
                        SyncSotoExteriorEntitySpawnS2CPayload.fromEntity(ctx.tardisId(), entity, ctx.footprintOrigin());
                if (spawn.typeId() == null || spawn.typeId().getPath().isEmpty()) {
                    continue;
                }
                ServerPlayNetworking.send(player, spawn);
                tracked.add(entity.getUUID());
            }
        }
        for (UUID id : Set.copyOf(tracked)) {
            if (!liveIds.contains(id)) {
                tracked.remove(id);
                ServerPlayNetworking.send(player, new SyncSotoExteriorEntityRemoveS2CPayload(ctx.tardisId(), id));
            }
        }
    }

    private static ExteriorContext resolveExterior(MinecraftServer server, UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return null;
        }
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(model.exteriorDimension));
        ServerLevel world = server.getLevel(worldKey);
        if (world == null) {
            return null;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        SotoExteriorIndex.register(tardisId, worldKey, exteriorPos);
        return new ExteriorContext(tardisId, world, exteriorPos, SotoExteriorSampler.footprintOrigin(exteriorPos));
    }

    private record ViewerKey(UUID tardisId, UUID playerId) {
    }

    private record ExteriorContext(UUID tardisId, ServerLevel world, BlockPos exteriorPos, BlockPos footprintOrigin) {
    }
}
