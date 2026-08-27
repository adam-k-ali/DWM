package com.adamkali.dwm.tardis.portal;

import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntityRemoveS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntitySpawnS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntityUpdateS2CPayload;
import com.adamkali.dwm.network.SyncPortalMetaS2CPayload;
import com.adamkali.dwm.network.SyncPortalPerfS2CPayload;
import com.adamkali.dwm.network.UnloadPortalChunkS2CPayload;
import com.adamkali.dwm.tardis.interior.TardisInteriorPreloadService;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
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
 * Shared BOTI/SOTO portal stream: meta (shell + atmosphere) + chunk + entity packets.
 */
public final class PortalStreamSyncService {
    private static final int META_FLUSH_INTERVAL_TICKS = 3;
    private static final int ENTITY_UPDATE_INTERVAL_TICKS = 2;
    private static final int VIEWER_LEAVE_GRACE_TICKS = 40;

    private static final Map<StreamKey, Set<UUID>> SUBSCRIBERS = new ConcurrentHashMap<>();
    private static final Map<ViewerKey, Set<Long>> SENT_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<ViewerKey, Set<UUID>> TRACKED_ENTITIES = new ConcurrentHashMap<>();
    private static final Map<StreamKey, Set<Long>> DIRTY_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<StreamKey, Integer> LEAVE_GRACE = new ConcurrentHashMap<>();
    private static final Set<UUID> META_DIRTY = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> META_REVISIONS = new ConcurrentHashMap<>();
    private static final Set<ServerPlayer> TICK_VIEWERS = ConcurrentHashMap.newKeySet();
    private static int tickCounter;

    private PortalStreamSyncService() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide()) {
                markDirtyAt(world.dimension(), pos);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide()) {
                markDirtyAt(world.dimension(), hitResult.getBlockPos());
                markDirtyAt(world.dimension(), hitResult.getBlockPos().relative(hitResult.getDirection()));
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(PortalStreamSyncService::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> clear());
    }

    public static void clear() {
        SUBSCRIBERS.clear();
        SENT_CHUNKS.clear();
        TRACKED_ENTITIES.clear();
        DIRTY_CHUNKS.clear();
        LEAVE_GRACE.clear();
        META_DIRTY.clear();
        META_REVISIONS.clear();
        TICK_VIEWERS.clear();
        SotoExteriorIndex.clear();
        BotiPlotIndex.clear();
        PortalStreamPerfStats.clear();
        TardisInteriorPreloadService.clear();
        tickCounter = 0;
    }

    public static void setMetaChanged(UUID tardisId) {
        if (tardisId != null) {
            META_DIRTY.add(tardisId);
        }
    }

    /**
     * Marks every BOTI footprint chunk column dirty so already-streamed empty samples are re-sent
     * after interior place / rebuild.
     */
    public static void markBotiFootprintDirty(UUID tardisId) {
        if (tardisId == null) {
            return;
        }
        META_DIRTY.add(tardisId);
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        int[] bounds = BotiInteriorSampler.footprintChunkBounds(origin);
        StreamKey streamKey = new StreamKey(PortalStreamKind.BOTI, tardisId);
        Set<Long> dirty = DIRTY_CHUNKS.computeIfAbsent(streamKey, id -> ConcurrentHashMap.newKeySet());
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                dirty.add(ChunkPos.pack(cx, cz));
            }
        }
    }

    /** Test helper: number of dirty BOTI footprint columns for {@code tardisId}. */
    public static int botiDirtyChunkCountForTest(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        Set<Long> dirty = DIRTY_CHUNKS.get(new StreamKey(PortalStreamKind.BOTI, tardisId));
        return dirty == null ? 0 : dirty.size();
    }

    /** @deprecated Prefer {@link #setMetaChanged(UUID)}. */
    @Deprecated
    public static void setChanged(UUID tardisId) {
        setMetaChanged(tardisId);
    }

    public static void markDirtyAt(ResourceKey<Level> worldKey, BlockPos worldPos) {
        if (worldKey == null || worldPos == null) {
            return;
        }
        UUID sotoId = SotoExteriorIndex.resolve(worldKey, worldPos);
        if (sotoId != null) {
            META_DIRTY.add(sotoId);
            markChunkDirty(PortalStreamKind.SOTO, sotoId, worldPos);
        }
        if (worldKey.equals(TardisDimensions.TARDIS_WORLD_KEY)) {
            UUID botiId = BotiPlotIndex.resolve(worldPos);
            if (botiId != null) {
                META_DIRTY.add(botiId);
                markChunkDirty(PortalStreamKind.BOTI, botiId, worldPos);
            }
        }
    }

    public static boolean subscribe(ServerPlayer player, PortalStreamKind kind, UUID tardisId) {
        if (player == null || kind == null || tardisId == null) {
            return false;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        SceneContext ctx = resolveScene(server, kind, tardisId);
        if (ctx == null) {
            return false;
        }
        StreamKey streamKey = new StreamKey(kind, tardisId);
        SUBSCRIBERS.computeIfAbsent(streamKey, id -> ConcurrentHashMap.newKeySet()).add(player.getUUID());
        LEAVE_GRACE.remove(streamKey);
        ViewerKey viewerKey = new ViewerKey(kind, tardisId, player.getUUID());
        Set<Long> alreadySent = SENT_CHUNKS.get(viewerKey);
        boolean alreadyStreaming = alreadySent != null && !alreadySent.isEmpty();
        if (alreadyStreaming) {
            return true;
        }
        sendMeta(player, ctx);
        sendFullChunks(player, ctx);
        return true;
    }

    private static void markChunkDirty(PortalStreamKind kind, UUID tardisId, BlockPos worldPos) {
        long packed = ChunkPos.pack(worldPos.getX() >> 4, worldPos.getZ() >> 4);
        DIRTY_CHUNKS.computeIfAbsent(new StreamKey(kind, tardisId), id -> ConcurrentHashMap.newKeySet()).add(packed);
    }

    private static void onEndTick(MinecraftServer server) {
        tickCounter++;
        TICK_VIEWERS.clear();
        boolean diag = PortalStreamPerfStats.isEnabled();
        if (diag) {
            PortalStreamPerfStats.beginTick(server);
        }
        TardisInteriorPreloadService.tick(server);
        markShellAnimatingDirty();
        long metaStart = PortalStreamPerfStats.begin();
        flushMeta(server);
        PortalStreamPerfStats.endFlushMeta(metaStart);
        long sotoStart = PortalStreamPerfStats.begin();
        flushStreams(server, PortalStreamKind.SOTO);
        PortalStreamPerfStats.endFlushSoto(sotoStart);
        long botiStart = PortalStreamPerfStats.begin();
        flushStreams(server, PortalStreamKind.BOTI);
        PortalStreamPerfStats.endFlushBoti(botiStart);
        if (diag) {
            PortalStreamPerfStats.endTick();
            maybeSendPerfDiag(server);
        }
    }

    private static void maybeSendPerfDiag(MinecraftServer server) {
        PortalStreamPerfStats.Snapshot snap = PortalStreamPerfStats.maybePublish(tickCounter);
        if (snap == null || !snap.isPresent() || TICK_VIEWERS.isEmpty()) {
            return;
        }
        SyncPortalPerfS2CPayload payload = SyncPortalPerfS2CPayload.fromSnapshot(snap);
        for (ServerPlayer viewer : Set.copyOf(TICK_VIEWERS)) {
            if (viewer != null && !viewer.hasDisconnected()) {
                ServerPlayNetworking.send(viewer, payload);
            }
        }
    }

    private static void markShellAnimatingDirty() {
        for (UUID tardisId : SotoExteriorIndex.registeredIds()) {
            TardisDataModel model = TardisDataLoader.get(tardisId);
            if (model == null || model.doorState == null) {
                continue;
            }
            float swing = model.doorState.doorSwing;
            if (swing > 0.0f && swing < 1.0f) {
                setMetaChanged(tardisId);
            }
        }
        for (UUID tardisId : BotiPlotIndex.registeredIds()) {
            TardisDataModel model = TardisDataLoader.get(tardisId);
            if (model == null || model.doorState == null) {
                continue;
            }
            float swing = model.doorState.doorSwing;
            if (swing > 0.0f && swing < 1.0f) {
                setMetaChanged(tardisId);
            }
        }
    }

    private static void flushMeta(MinecraftServer server) {
        if (META_DIRTY.isEmpty() || tickCounter % META_FLUSH_INTERVAL_TICKS != 0) {
            return;
        }
        Set<UUID> toFlush = Set.copyOf(META_DIRTY);
        META_DIRTY.removeAll(toFlush);
        for (UUID tardisId : toFlush) {
            for (PortalStreamKind kind : PortalStreamKind.values()) {
                SceneContext ctx = resolveScene(server, kind, tardisId);
                if (ctx == null) {
                    continue;
                }
                Set<ServerPlayer> viewers = collectViewers(server, kind, tardisId);
                if (viewers.isEmpty()) {
                    continue;
                }
                int revision = META_REVISIONS.merge(tardisId, 1, Integer::sum);
                SyncPortalMetaS2CPayload payload = SyncPortalMetaS2CPayload.of(
                        kind, tardisId, revision, ctx.shell(), ctx.atmosphere()
                );
                for (ServerPlayer viewer : viewers) {
                    ServerPlayNetworking.send(viewer, payload);
                    PortalStreamPerfStats.noteMetaPacket();
                }
            }
        }
    }

    private static void flushStreams(MinecraftServer server, PortalStreamKind kind) {
        Set<UUID> registered = kind == PortalStreamKind.SOTO
                ? SotoExteriorIndex.registeredIds()
                : BotiPlotIndex.registeredIds();
        Set<UUID> active = new HashSet<>();
        Set<StreamKey> dirtyFlushed = new HashSet<>();
        for (UUID tardisId : registered) {
            StreamKey streamKey = new StreamKey(kind, tardisId);
            Set<ServerPlayer> viewers = collectViewers(server, kind, tardisId);
            if (viewers.isEmpty()) {
                handleNoViewers(server, streamKey);
                continue;
            }
            active.add(tardisId);
            TICK_VIEWERS.addAll(viewers);
            PortalStreamPerfStats.noteStreamViewers(viewers.size());
            LEAVE_GRACE.remove(streamKey);
            SceneContext ctx = resolveScene(server, kind, tardisId);
            if (ctx == null) {
                continue;
            }
            keepAlive(ctx);
            if (kind == PortalStreamKind.BOTI
                    && !BotiInteriorSampler.isFootprintLightReady(ctx.world(), ctx.footprintOrigin())) {
                continue;
            }
            for (ServerPlayer viewer : viewers) {
                ensureChunks(viewer, ctx);
                flushDirtyChunks(viewer, ctx);
            }
            dirtyFlushed.add(streamKey);
            if (tickCounter % ENTITY_UPDATE_INTERVAL_TICKS == 0) {
                for (ServerPlayer viewer : viewers) {
                    syncEntities(viewer, ctx);
                }
            }
        }
        for (StreamKey key : dirtyFlushed) {
            DIRTY_CHUNKS.remove(key);
        }
        SUBSCRIBERS.keySet().removeIf(key ->
                key.kind() == kind && !registered.contains(key.tardisId()) && !active.contains(key.tardisId()));
    }

    private static void handleNoViewers(MinecraftServer server, StreamKey streamKey) {
        int grace = LEAVE_GRACE.merge(streamKey, 1, Integer::sum);
        if (grace < VIEWER_LEAVE_GRACE_TICKS) {
            return;
        }
        unloadAllViewers(server, streamKey);
        SUBSCRIBERS.remove(streamKey);
        DIRTY_CHUNKS.remove(streamKey);
        LEAVE_GRACE.remove(streamKey);
    }

    private static void unloadAllViewers(MinecraftServer server, StreamKey streamKey) {
        Set<ViewerKey> keys = new HashSet<>();
        for (ViewerKey key : SENT_CHUNKS.keySet()) {
            if (key.kind == streamKey.kind && key.tardisId.equals(streamKey.tardisId)) {
                keys.add(key);
            }
        }
        for (ViewerKey key : TRACKED_ENTITIES.keySet()) {
            if (key.kind == streamKey.kind && key.tardisId.equals(streamKey.tardisId)) {
                keys.add(key);
            }
        }
        for (ViewerKey key : keys) {
            ServerPlayer player = server.getPlayerList().getPlayer(key.playerId);
            if (player != null) {
                for (long packed : SENT_CHUNKS.getOrDefault(key, Set.of())) {
                    ServerPlayNetworking.send(player, new UnloadPortalChunkS2CPayload(
                            streamKey.kind, streamKey.tardisId, ChunkPos.getX(packed), ChunkPos.getZ(packed)
                    ));
                }
                for (UUID entityId : TRACKED_ENTITIES.getOrDefault(key, Set.of())) {
                    ServerPlayNetworking.send(player, new SyncPortalEntityRemoveS2CPayload(
                            streamKey.kind, streamKey.tardisId, entityId
                    ));
                    PortalStreamPerfStats.noteEntityRemove();
                }
            }
            SENT_CHUNKS.remove(key);
            TRACKED_ENTITIES.remove(key);
        }
    }

    private static Set<ServerPlayer> collectViewers(MinecraftServer server, PortalStreamKind kind, UUID tardisId) {
        Set<ServerPlayer> viewers = ConcurrentHashMap.newKeySet();
        if (kind == PortalStreamKind.SOTO) {
            ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
            if (interiorWorld != null) {
                BlockPos doorOrigin = TardisPlotAllocator.plotOrigin(tardisId)
                        .offset(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
                viewers.addAll(PlayerLookup.tracking(interiorWorld, doorOrigin));
            }
        } else {
            TardisDataModel model = TardisDataLoader.get(tardisId);
            if (model != null && model.hasExteriorLocation && model.exteriorDimension != null) {
                ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(model.exteriorDimension));
                ServerLevel exteriorWorld = server.getLevel(worldKey);
                if (exteriorWorld != null) {
                    BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
                    viewers.addAll(PlayerLookup.tracking(exteriorWorld, exteriorPos));
                }
            }
        }
        Set<UUID> subscribers = SUBSCRIBERS.get(new StreamKey(kind, tardisId));
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

    private static void sendMeta(ServerPlayer player, SceneContext ctx) {
        int revision = META_REVISIONS.merge(ctx.tardisId(), 1, Integer::sum);
        ServerPlayNetworking.send(player, SyncPortalMetaS2CPayload.of(
                ctx.kind(), ctx.tardisId(), revision, ctx.shell(), ctx.atmosphere()
        ));
        PortalStreamPerfStats.noteMetaPacket();
    }

    private static void sendFullChunks(ServerPlayer player, SceneContext ctx) {
        int[] bounds = ctx.chunkBounds();
        ViewerKey key = new ViewerKey(ctx.kind(), ctx.tardisId(), player.getUUID());
        Set<Long> sent = SENT_CHUNKS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                PortalStreamSample sample = ctx.sampleChunk(cx, cz);
                PortalStreamPerfStats.noteChunkSample();
                ServerPlayNetworking.send(
                        player,
                        SyncPortalChunkS2CPayload.fromSample(ctx.kind(), ctx.tardisId(), ctx.footprintOrigin(), sample)
                );
                PortalStreamPerfStats.noteChunkPacket();
                sent.add(ChunkPos.pack(cx, cz));
            }
        }
        PortalStreamPerfStats.noteFullResync();
        TRACKED_ENTITIES.put(key, ConcurrentHashMap.newKeySet());
        syncEntities(player, ctx);
    }

    private static void ensureChunks(ServerPlayer player, SceneContext ctx) {
        ViewerKey key = new ViewerKey(ctx.kind(), ctx.tardisId(), player.getUUID());
        Set<Long> sent = SENT_CHUNKS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        int[] bounds = ctx.chunkBounds();
        Set<Long> desired = new HashSet<>();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                desired.add(ChunkPos.pack(cx, cz));
            }
        }
        for (long packed : Set.copyOf(sent)) {
            if (!desired.contains(packed)) {
                sent.remove(packed);
                ServerPlayNetworking.send(player, new UnloadPortalChunkS2CPayload(
                        ctx.kind(), ctx.tardisId(), ChunkPos.getX(packed), ChunkPos.getZ(packed)
                ));
            }
        }
        for (long packed : desired) {
            if (sent.contains(packed)) {
                continue;
            }
            int cx = ChunkPos.getX(packed);
            int cz = ChunkPos.getZ(packed);
            PortalStreamSample sample = ctx.sampleChunk(cx, cz);
            PortalStreamPerfStats.noteChunkSample();
            ServerPlayNetworking.send(
                    player,
                    SyncPortalChunkS2CPayload.fromSample(ctx.kind(), ctx.tardisId(), ctx.footprintOrigin(), sample)
            );
            PortalStreamPerfStats.noteChunkPacket();
            sent.add(packed);
        }
    }

    private static void flushDirtyChunks(ServerPlayer player, SceneContext ctx) {
        Set<Long> dirty = DIRTY_CHUNKS.get(new StreamKey(ctx.kind(), ctx.tardisId()));
        if (dirty == null || dirty.isEmpty()) {
            return;
        }
        ViewerKey key = new ViewerKey(ctx.kind(), ctx.tardisId(), player.getUUID());
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
            PortalStreamSample sample = ctx.sampleChunk(cx, cz);
            PortalStreamPerfStats.noteChunkSample();
            ServerPlayNetworking.send(
                    player,
                    SyncPortalChunkS2CPayload.fromSample(ctx.kind(), ctx.tardisId(), ctx.footprintOrigin(), sample)
            );
            PortalStreamPerfStats.noteChunkPacket();
        }
    }

    private static void syncEntities(ServerPlayer player, SceneContext ctx) {
        long syncStart = PortalStreamPerfStats.begin();
        ViewerKey key = new ViewerKey(ctx.kind(), ctx.tardisId(), player.getUUID());
        Set<UUID> tracked = TRACKED_ENTITIES.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        List<Entity> live = ctx.collectEntities();
        PortalStreamPerfStats.noteEntitiesScanned(live.size());
        Set<UUID> liveIds = new HashSet<>();
        for (Entity entity : live) {
            if (BotiInteriorSampler.captureEntityNbt(entity) == null
                    && !(entity instanceof net.minecraft.world.entity.player.Player)) {
                continue;
            }
            liveIds.add(entity.getUUID());
            if (tracked.contains(entity.getUUID())) {
                ServerPlayNetworking.send(
                        player,
                        SyncPortalEntityUpdateS2CPayload.fromEntity(ctx.kind(), ctx.tardisId(), entity, ctx.footprintOrigin())
                );
                PortalStreamPerfStats.noteEntityUpdate();
            } else {
                SyncPortalEntitySpawnS2CPayload spawn =
                        SyncPortalEntitySpawnS2CPayload.fromEntity(ctx.kind(), ctx.tardisId(), entity, ctx.footprintOrigin());
                if (spawn.typeId() == null || spawn.typeId().getPath().isEmpty()) {
                    continue;
                }
                ServerPlayNetworking.send(player, spawn);
                PortalStreamPerfStats.noteEntitySpawn();
                tracked.add(entity.getUUID());
            }
        }
        for (UUID id : Set.copyOf(tracked)) {
            if (!liveIds.contains(id)) {
                tracked.remove(id);
                ServerPlayNetworking.send(player, new SyncPortalEntityRemoveS2CPayload(ctx.kind(), ctx.tardisId(), id));
                PortalStreamPerfStats.noteEntityRemove();
            }
        }
        PortalStreamPerfStats.endSyncEntities(syncStart);
    }

    private static void keepAlive(SceneContext ctx) {
        if (ctx.kind() == PortalStreamKind.SOTO) {
            SotoExteriorSampler.addStreamTickets(ctx.world(), ctx.anchorPos());
            SotoExteriorSampler.keepMobAiActive(ctx.world(), ctx.anchorPos());
        } else {
            // Ticket-only while streaming; force-load only when sampling entities needs it.
            BotiInteriorSampler.addFootprintTickets(ctx.world(), ctx.footprintOrigin());
            BotiInteriorSampler.keepMobAiActive(ctx.world(), ctx.tardisId());
        }
    }

    private static SceneContext resolveScene(MinecraftServer server, PortalStreamKind kind, UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return null;
        }
        TardisDoorState doorState = model.doorState == null ? new TardisDoorState() : model.doorState;
        TardisChameleonVariant variant =
                model.variant == null ? TardisChameleonVariant.TT_CAPSULE : model.variant;
        PortalShellState shell = new PortalShellState(
                variant, doorState.doorSwing, doorState.isOpen, model.exteriorRotation
        );

        if (kind == PortalStreamKind.SOTO) {
            if (!model.hasExteriorLocation || model.exteriorDimension == null) {
                return null;
            }
            ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(model.exteriorDimension));
            ServerLevel world = server.getLevel(worldKey);
            if (world == null) {
                return null;
            }
            BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
            SotoExteriorIndex.register(tardisId, worldKey, exteriorPos);
            BlockPos footprintOrigin = SotoExteriorSampler.footprintOrigin(exteriorPos);
            PortalAtmosphere atmosphere = SotoExteriorSampler.sampleAtmosphere(world, exteriorPos);
            return new SceneContext(kind, tardisId, world, exteriorPos, footprintOrigin, shell, atmosphere);
        }

        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return null;
        }
        BotiPlotIndex.register(tardisId);
        BlockPos plotOrigin = TardisPlotAllocator.plotOrigin(tardisId);
        PortalAtmosphere atmosphere = BotiInteriorSampler.sampleAtmosphere(interior, plotOrigin);
        return new SceneContext(kind, tardisId, interior, plotOrigin, plotOrigin, shell, atmosphere);
    }

    private record StreamKey(PortalStreamKind kind, UUID tardisId) {
    }

    private record ViewerKey(PortalStreamKind kind, UUID tardisId, UUID playerId) {
    }

    private record SceneContext(
            PortalStreamKind kind,
            UUID tardisId,
            ServerLevel world,
            BlockPos anchorPos,
            BlockPos footprintOrigin,
            PortalShellState shell,
            PortalAtmosphere atmosphere
    ) {
        int[] chunkBounds() {
            if (kind == PortalStreamKind.SOTO) {
                return SotoExteriorSampler.streamChunkBounds(anchorPos);
            }
            return BotiInteriorSampler.footprintChunkBounds(footprintOrigin);
        }

        PortalStreamSample sampleChunk(int chunkX, int chunkZ) {
            if (kind == PortalStreamKind.SOTO) {
                return SotoExteriorSampler.samplePortalStreamChunk(world, anchorPos, chunkX, chunkZ);
            }
            return BotiInteriorSampler.sampleStreamChunk(world, tardisId, chunkX, chunkZ);
        }

        List<Entity> collectEntities() {
            if (kind == PortalStreamKind.SOTO) {
                return SotoExteriorSampler.collectStreamEntities(world, anchorPos);
            }
            return BotiInteriorSampler.collectStreamEntities(world, tardisId);
        }
    }
}
