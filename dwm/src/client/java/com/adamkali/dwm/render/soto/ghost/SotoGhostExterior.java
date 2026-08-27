package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntitySpawnS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntityUpdateS2CPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion;
import com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.render.portal.PortalFrameCache;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-TARDIS ghost exterior: streamed chunks + live entities in footprint-relative space.
 * Entity poses are packet-interpolated (no client velocity integration) to avoid snap/jitter.
 * Keyed by (PortalStreamKind, UUID) so BOTI and SOTO share the same store.
 */
public final class SotoGhostExterior implements BlockAndTintGetter {
    private static final String PLAYER_ENTITY_ID = "minecraft:player";
    /** Matches server ghost entity update cadence (every 2 ticks @ 20 TPS). */
    public static final long ENTITY_UPDATE_INTERVAL_MS = 100L;
    private static final int BOTTOM_Y = -64;
    private static final int HEIGHT = 384;
    private static final AtomicInteger NEXT_GHOST_ENTITY_ID = new AtomicInteger(1_000_000);

    private static final Map<PortalSceneStore.SceneKey, SotoGhostExterior> BY_KEY = new ConcurrentHashMap<>();
    /** Survives remove+respawn flaps so item bob/spin phase does not reset. */
    private static final Map<UUID, Float> ITEM_BOB_OFFSETS = new ConcurrentHashMap<>();

    private final PortalStreamKind kind;
    private final UUID tardisId;
    private BlockPos footprintOrigin = BlockPos.ZERO;
    private final Map<Long, Map<BlockPos, BlockState>> chunkBlocks = new ConcurrentHashMap<>();
    private final Map<Long, Map<BlockPos, CompoundTag>> chunkBlockEntities = new ConcurrentHashMap<>();
    private final Map<Long, PortalLightData> chunkLights = new ConcurrentHashMap<>();
    private final Map<BlockPos, BlockState> blocksByRel = new ConcurrentHashMap<>();
    private final Map<BlockPos, CompoundTag> blockEntitiesByRel = new ConcurrentHashMap<>();
    private final Map<UUID, GhostEntity> entities = new ConcurrentHashMap<>();

    private SotoGhostExterior(PortalStreamKind kind, UUID tardisId) {
        this.kind = kind;
        this.tardisId = tardisId;
    }

    public static SotoGhostExterior get(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return null;
        }
        return BY_KEY.get(new PortalSceneStore.SceneKey(kind, tardisId));
    }

    public static SotoGhostExterior getOrCreate(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return null;
        }
        PortalSceneStore.SceneKey key = new PortalSceneStore.SceneKey(kind, tardisId);
        return BY_KEY.computeIfAbsent(key, k -> new SotoGhostExterior(k.kind(), k.tardisId()));
    }

    public static boolean hasEntities(PortalStreamKind kind, UUID tardisId) {
        SotoGhostExterior ghost = get(kind, tardisId);
        return ghost != null && !ghost.entities.isEmpty();
    }

    /**
     * Entities with packet-lerped poses for smooth portal rendering.
     */
    public static List<RenderableGhostEntity> getRenderableEntities(PortalStreamKind kind, UUID tardisId) {
        return getRenderableEntities(kind, tardisId, Util.getMillis());
    }

    /**
     * Same as {@link #getRenderableEntities(PortalStreamKind, UUID)} with a fixed clock (tests / deterministic lerp).
     */
    public static List<RenderableGhostEntity> getRenderableEntities(
            PortalStreamKind kind,
            UUID tardisId,
            long nowMs
    ) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null || ghost.entities.isEmpty()) {
            return List.of();
        }
        List<RenderableGhostEntity> result = new ArrayList<>(ghost.entities.size());
        for (Map.Entry<UUID, GhostEntity> entry : ghost.entities.entrySet()) {
            GhostEntity ghostEntity = entry.getValue();
            if (ghostEntity.interp == null) {
                continue;
            }
            LerpedPose pose = BotiEntityMotion.lerpPose(ghostEntity.interp, nowMs, ENTITY_UPDATE_INTERVAL_MS);
            // Stats only: once-per-frame baseline lives in PortalPerfStats (not on GhostEntity),
            // so repeated getRenderableEntities calls in one flush cannot corrupt inter-frame deltas.
            PortalPerfStats.noteLerpedPose(
                    kind,
                    tardisId,
                    entry.getKey(),
                    pose.x(),
                    pose.y(),
                    pose.z()
            );
            if (ghostEntity.entity == null) {
                continue;
            }
            float animAge = animAgeInTicks(ghostEntity.animStartMs, nowMs);
            result.add(new RenderableGhostEntity(
                    ghostEntity.entity,
                    pose,
                    animAge,
                    ghostEntity.bobOffset
            ));
        }
        return List.copyOf(result);
    }

    /**
     * Test helper: stores interp without a live entity so pose sampling can be asserted headlessly.
     */
    static void putInterpForTest(
            PortalStreamKind kind,
            UUID tardisId,
            UUID entityUuid,
            EntityInterpState interp
    ) {
        if (kind == null || tardisId == null || entityUuid == null || interp == null) {
            return;
        }
        getOrCreate(kind, tardisId).entities.put(entityUuid, new GhostEntity(null, interp, interp.receiveTimeMs(), 0.0f));
    }

    /**
     * Test helper: advances stored interp the same way {@link #applyEntityUpdate} does (no position snap).
     */
    static void advanceInterpForTest(
            PortalStreamKind kind,
            UUID tardisId,
            UUID entityUuid,
            float relX,
            float relY,
            float relZ,
            float yaw,
            float pitch,
            long receiveTimeMs
    ) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null || entityUuid == null) {
            return;
        }
        GhostEntity previous = ghost.entities.get(entityUuid);
        EntityInterpState next = previous == null || previous.interp == null
                ? EntityInterpState.identity(relX, relY, relZ, yaw, pitch, receiveTimeMs)
                : nextInterpForUpdate(previous.entity, previous.interp, relX, relY, relZ, yaw, pitch, receiveTimeMs);
        Entity entity = previous == null ? null : previous.entity;
        GhostEntity stored = new GhostEntity(entity, next, animStartMsPreserved(previous, receiveTimeMs), previous != null ? previous.bobOffset : 0.0f);
        ghost.entities.put(entityUuid, stored);
    }

    /**
     * Test helper: lerped poses for all ghosts at {@code nowMs} (includes interp-only test entries).
     */
    static List<LerpedPose> sampleLerpedPosesForTest(PortalStreamKind kind, UUID tardisId, long nowMs) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null || ghost.entities.isEmpty()) {
            return List.of();
        }
        List<LerpedPose> result = new ArrayList<>(ghost.entities.size());
        for (GhostEntity ghostEntity : ghost.entities.values()) {
            if (ghostEntity.interp == null) {
                continue;
            }
            result.add(BotiEntityMotion.lerpPose(ghostEntity.interp, nowMs, ENTITY_UPDATE_INTERVAL_MS));
        }
        return List.copyOf(result);
    }

    public static void invalidate(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        PortalSceneStore.SceneKey key = new PortalSceneStore.SceneKey(kind, tardisId);
        SotoGhostExterior removed = BY_KEY.remove(key);
        if (removed != null) {
            for (UUID entityUuid : removed.entities.keySet()) {
                ITEM_BOB_OFFSETS.remove(entityUuid);
            }
        }
        PortalPerfStats.clearPoseTracking(kind, tardisId);
        SotoGhostMeshCache.invalidate(kind, tardisId);
    }

    public static void invalidateAll() {
        BY_KEY.clear();
        ITEM_BOB_OFFSETS.clear();
        PortalPerfStats.clearAllPoseTracking();
        SotoGhostMeshCache.invalidateAll();
    }

    public static void clientTick() {
        for (SotoGhostExterior ghost : BY_KEY.values()) {
            ghost.tickEntities();
            if (ghost.entityCount() > 0) {
                PortalFrameCache.markDirty(ghost.kind, ghost.tardisId);
            }
        }
    }

    public static void applyChunk(PortalStreamKind kind, SyncPortalChunkS2CPayload payload) {
        if (payload == null || payload.tardisId() == null) {
            return;
        }
        SotoGhostExterior ghost = getOrCreate(kind, payload.tardisId());
        ghost.footprintOrigin = payload.footprintOrigin();
        long key = ChunkPos.pack(payload.chunkX(), payload.chunkZ());
        Map<BlockPos, BlockState> newBlocks = new HashMap<>(payload.toBlockMap());
        Map<BlockPos, CompoundTag> newBes = new HashMap<>(payload.toBlockEntityMap());
        PortalLightData newLight = payload.lightData();
        // #region agent log
        try {
            int firstPacked = newLight.isEmpty() ? -1 : newLight.packed(newLight.min());
            int matchedBlocks = 0, maxMatchedBlock = 0, maxMatchedSky = 0;
            for (BlockPos pos : newBlocks.keySet()) {
                int packed = newLight.packed(pos);
                if (packed >= 0) {
                    matchedBlocks++;
                    maxMatchedBlock = Math.max(maxMatchedBlock, packed & 0xF);
                    maxMatchedSky = Math.max(maxMatchedSky, packed >>> 4);
                }
            }
            java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"),
                    "{\"hypothesisId\":\"C\",\"location\":\"SotoGhostExterior.applyChunk\",\"message\":\"client received portal light\",\"data\":{\"kind\":\"" + kind + "\",\"chunkX\":" + payload.chunkX() + ",\"chunkZ\":" + payload.chunkZ() + ",\"blocks\":" + newBlocks.size() + ",\"originX\":" + payload.footprintOriginX() + ",\"originY\":" + payload.footprintOriginY() + ",\"originZ\":" + payload.footprintOriginZ() + ",\"lightMinX\":" + newLight.min().getX() + ",\"lightMinY\":" + newLight.min().getY() + ",\"lightMinZ\":" + newLight.min().getZ() + ",\"lightBytes\":" + newLight.packedCopy().length + ",\"firstPacked\":" + firstPacked + ",\"matchedBlocks\":" + matchedBlocks + ",\"maxMatchedBlock\":" + maxMatchedBlock + ",\"maxMatchedSky\":" + maxMatchedSky + "},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException ignored) {
        }
        // #endregion
        Map<BlockPos, BlockState> previousBlocks = ghost.chunkBlocks.get(key);
        Map<BlockPos, CompoundTag> previousBes = ghost.chunkBlockEntities.get(key);
        PortalLightData previousLight = ghost.chunkLights.get(key);
        boolean contentUnchanged = chunkContentUnchanged(
                previousBlocks, newBlocks, previousBes, newBes, previousLight, newLight
        );
        boolean hasDrawable = SotoGhostMeshCache.hasDrawableChunk(
                kind, payload.tardisId(), payload.chunkX(), payload.chunkZ()
        );
        if (contentUnchanged && hasDrawable) {
            // Keep maps current (cheap) but skip tessellation / pass-batch rebuild.
            ghost.chunkBlocks.put(key, newBlocks);
            ghost.chunkBlockEntities.put(key, newBes);
            ghost.chunkLights.put(key, newLight);
            PortalPerfStats.noteBakeSkip();
            return;
        }
        previousBlocks = ghost.chunkBlocks.put(key, newBlocks);
        previousBes = ghost.chunkBlockEntities.put(key, newBes);
        ghost.chunkLights.put(key, newLight);
        if (previousBlocks != null) {
            for (BlockPos pos : previousBlocks.keySet()) {
                ghost.blocksByRel.remove(pos);
            }
        }
        if (previousBes != null) {
            for (BlockPos pos : previousBes.keySet()) {
                ghost.blockEntitiesByRel.remove(pos);
            }
        }
        ghost.blocksByRel.putAll(newBlocks);
        ghost.blockEntitiesByRel.putAll(newBes);
        ghost.rebuildChunkAndLightNeighbors(payload.chunkX(), payload.chunkZ());
    }

    /**
     * True when block + block-entity maps are equal (including both null/empty).
     * Package-visible for unit tests.
     */
    static boolean chunkContentUnchanged(
            Map<BlockPos, BlockState> previousBlocks,
            Map<BlockPos, BlockState> newBlocks,
            Map<BlockPos, CompoundTag> previousBes,
            Map<BlockPos, CompoundTag> newBes,
            PortalLightData previousLight,
            PortalLightData newLight
    ) {
        return Objects.equals(emptyToNull(previousBlocks), emptyToNull(newBlocks))
                && Objects.equals(emptyToNull(previousBes), emptyToNull(newBes))
                && Objects.equals(previousLight, newLight);
    }

    private void rebuildChunkAndLightNeighbors(int chunkX, int chunkZ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int affectedX = chunkX + dx;
                int affectedZ = chunkZ + dz;
                if (chunkBlocks.containsKey(ChunkPos.pack(affectedX, affectedZ))) {
                    SotoGhostMeshCache.onChunkApplied(kind, tardisId, affectedX, affectedZ, this);
                }
            }
        }
    }

    private static <K, V> Map<K, V> emptyToNull(Map<K, V> map) {
        return map == null || map.isEmpty() ? null : map;
    }

    public static void unloadChunk(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null) {
            return;
        }
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<BlockPos, BlockState> removedBlocks = ghost.chunkBlocks.remove(key);
        Map<BlockPos, CompoundTag> removedBes = ghost.chunkBlockEntities.remove(key);
        ghost.chunkLights.remove(key);
        if (removedBlocks != null) {
            for (BlockPos pos : removedBlocks.keySet()) {
                ghost.blocksByRel.remove(pos);
            }
        }
        if (removedBes != null) {
            for (BlockPos pos : removedBes.keySet()) {
                ghost.blockEntitiesByRel.remove(pos);
            }
        }
        SotoGhostMeshCache.onChunkUnloaded(kind, tardisId, chunkX, chunkZ);
        ghost.rebuildChunkAndLightNeighbors(chunkX, chunkZ);
    }

    public static void applyEntitySpawn(PortalStreamKind kind, SyncPortalEntitySpawnS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.entityUuid() == null) {
            return;
        }
        SotoGhostExterior ghost = getOrCreate(kind, payload.tardisId());
        GhostEntity previous = ghost.entities.get(payload.entityUuid());
        long now = Util.getMillis();
        // Duplicate spawn for an existing ghost: reuse the entity so ItemEntity.bobOffs stays stable.
        if (previous != null && previous.entity != null) {
            Entity entity = previous.entity;
            EntityInterpState interp = EntityInterpState.identity(
                    payload.relX(), payload.relY(), payload.relZ(), payload.yaw(), payload.pitch(), now
            );
            ghost.snapEntityPose(
                    entity,
                    payload.relX(),
                    payload.relY(),
                    payload.relZ(),
                    payload.yaw(),
                    payload.pitch(),
                    payload.headYaw(),
                    payload.bodyYaw()
            );
            GhostEntity stored = new GhostEntity(
                    entity,
                    interp,
                    previous.animStartMs,
                    previous.bobOffset
            );
            ghost.entities.put(payload.entityUuid(), stored);
            return;
        }
        Entity entity = ghost.createEntity(payload);
        if (entity == null) {
            return;
        }
        EntityInterpState interp = EntityInterpState.identity(
                payload.relX(), payload.relY(), payload.relZ(), payload.yaw(), payload.pitch(), now
        );
        ghost.snapEntityPose(
                entity,
                payload.relX(),
                payload.relY(),
                payload.relZ(),
                payload.yaw(),
                payload.pitch(),
                payload.headYaw(),
                payload.bodyYaw()
        );
        float bobOffset = entity instanceof ItemEntity item
                ? ITEM_BOB_OFFSETS.computeIfAbsent(payload.entityUuid(), id -> item.bobOffs)
                : 0.0f;
        long animStartMs = animStartMsForNew(entity, now);
        GhostEntity stored = new GhostEntity(entity, interp, animStartMs, bobOffset);
        ghost.entities.put(payload.entityUuid(), stored);
    }

    public static void applyEntityUpdate(PortalStreamKind kind, SyncPortalEntityUpdateS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.entityUuid() == null) {
            return;
        }
        SotoGhostExterior ghost = get(kind, payload.tardisId());
        if (ghost == null) {
            return;
        }
        GhostEntity previous = ghost.entities.get(payload.entityUuid());
        if (previous == null || previous.entity == null) {
            return;
        }
        long now = Util.getMillis();
        EntityInterpState next = previous.interp == null
                ? EntityInterpState.identity(
                        payload.relX(), payload.relY(), payload.relZ(), payload.yaw(), payload.pitch(), now)
                : nextInterpForUpdate(
                        previous.entity,
                        previous.interp,
                        payload.relX(),
                        payload.relY(),
                        payload.relZ(),
                        payload.yaw(),
                        payload.pitch(),
                        now);

        Entity entity = previous.entity;
        if (entity instanceof LivingEntity living) {
            float speed = BotiEntityMotion.limbSpeed(next.fromX(), next.fromZ(), next.toX(), next.toZ());
            living.walkAnimation.update(speed, 0.4f, living.isBaby() ? 3.0f : 1.0f);
            // Head/body only — position is packet-lerped at render time (avoid snap-to-target vs extract mismatch).
            living.setYBodyRot(payload.bodyYaw());
            living.setYHeadRot(payload.headYaw());
            living.yBodyRotO = payload.bodyYaw();
            living.yHeadRotO = payload.headYaw();
        }
        GhostEntity stored = new GhostEntity(entity, next, previous.animStartMs, previous.bobOffset);
        ghost.entities.put(payload.entityUuid(), stored);
    }

    /**
     * Items hold pose without micro-lerp (age-based bob/spin); others advance over the update interval.
     * Package-visible for tests.
     */
    static EntityInterpState nextInterpForUpdate(
            Entity entity,
            EntityInterpState previous,
            float relX,
            float relY,
            float relZ,
            float yaw,
            float pitch,
            long now
    ) {
        if (previous == null) {
            PortalPerfStats.noteIdentityInterp();
            return EntityInterpState.identity(relX, relY, relZ, yaw, pitch, now);
        }
        if (usesIdentityInterp(entity)) {
            PortalPerfStats.noteIdentityInterp();
            return EntityInterpState.identity(relX, relY, relZ, yaw, pitch, now);
        }
        PortalPerfStats.noteAdvanceInterp();
        return previous.advanceTo(relX, relY, relZ, yaw, pitch, now);
    }

    /** Package-visible: item ghosts skip packet position lerp. */
    static boolean usesIdentityInterp(Entity entity) {
        return entity instanceof ItemEntity;
    }

    /**
     * Wall-clock animation age in ticks (50ms). Independent of {@link Entity#tickCount}, which does not
     * advance for ghosts unless they are in {@link ClientLevel}'s ticker.
     */
    static float animAgeInTicks(long animStartMs, long nowMs) {
        if (animStartMs <= 0L || nowMs <= animStartMs) {
            return 0.0f;
        }
        return (nowMs - animStartMs) / 50.0f;
    }

    static long animStartMsForNew(Entity entity, long nowMs) {
        if (entity instanceof ItemEntity item) {
            int age = Math.max(0, item.getAge());
            return nowMs - (long) age * 50L;
        }
        return nowMs;
    }

    private static long animStartMsPreserved(GhostEntity previous, long nowMs) {
        return previous != null && previous.animStartMs > 0L ? previous.animStartMs : nowMs;
    }

    public static void removeEntity(PortalStreamKind kind, UUID tardisId, UUID entityUuid) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null || entityUuid == null) {
            return;
        }
        ghost.entities.remove(entityUuid);
        PortalPerfStats.clearPoseTracking(kind, tardisId, entityUuid);
    }

    public PortalStreamKind kind() {
        return kind;
    }

    public UUID tardisId() {
        return tardisId;
    }

    public BlockPos footprintOrigin() {
        return footprintOrigin;
    }

    public int chunkCount() {
        return chunkBlocks.size();
    }

    public int entityCount() {
        return entities.size();
    }

    public Set<Long> chunkKeys() {
        return Set.copyOf(chunkBlocks.keySet());
    }

    /** Blocks currently stored for one streamed chunk column (footprint-relative keys). */
    public Map<BlockPos, BlockState> blocksInChunk(long chunkKey) {
        Map<BlockPos, BlockState> blocks = chunkBlocks.get(chunkKey);
        return blocks == null || blocks.isEmpty() ? Map.of() : Map.copyOf(blocks);
    }

    public Map<BlockPos, CompoundTag> blockEntityNbtView() {
        return Map.copyOf(blockEntitiesByRel);
    }

    public Map<BlockPos, BlockState> blocksView() {
        return Map.copyOf(blocksByRel);
    }

    public boolean hasLightData() {
        return !chunkLights.isEmpty();
    }

    public int packedLight(BlockPos pos) {
        int block = getBrightness(LightLayer.BLOCK, pos);
        int sky = getBrightness(LightLayer.SKY, pos);
        return net.minecraft.util.LightCoordsUtil.pack(block, sky);
    }

    /**
     * Synthetic block entities for ghost BE rendering (same approach as snapshot mesh cache).
     */
    public List<BlockEntity> buildRenderedBlockEntities() {
        if (blockEntitiesByRel.isEmpty()) {
            return List.of();
        }
        Minecraft client = Minecraft.getInstance();
        Level world = client == null ? null : client.level;
        if (world == null) {
            return List.of();
        }
        List<BlockEntity> result = new ArrayList<>(blockEntitiesByRel.size());
        for (Map.Entry<BlockPos, CompoundTag> entry : blockEntitiesByRel.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = blocksByRel.get(pos);
            if (state == null || entry.getValue() == null) {
                continue;
            }
            BlockEntity be = BlockEntity.loadStatic(pos, state, entry.getValue(), world.registryAccess());
            if (be == null) {
                continue;
            }
            be.setLevel(world);
            result.add(be);
        }
        return List.copyOf(result);
    }

    /**
     * Advances animation age only. Position is packet-driven + render-lerped (no velocity integrate).
     * Also mirrors age onto {@link Entity#tickCount} so extract-based anims stay coherent if used.
     */
    private void tickEntities() {
        for (GhostEntity ghostEntity : entities.values()) {
            Entity entity = ghostEntity.entity;
            if (entity == null) {
                continue;
            }
            entity.tickCount = Math.max(entity.tickCount + 1, (int) animAgeInTicks(ghostEntity.animStartMs, Util.getMillis()));
        }
    }

    private Entity createEntity(SyncPortalEntitySpawnS2CPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !(client.level instanceof ClientLevel clientWorld)) {
            return null;
        }
        try {
            CompoundTag nbt = payload.nbt() == null ? new CompoundTag() : payload.nbt().copy();
            Identifier typeId = payload.typeId();
            String id = typeId == null ? nbt.getStringOr("id", "") : typeId.toString();
            Entity entity;
            if (PLAYER_ENTITY_ID.equals(id)) {
                UUID profileId = nbt.read(BotiEntitySample.BOTI_PROFILE_ID, UUIDUtil.CODEC)
                        .orElse(payload.entityUuid());
                String name = nbt.getStringOr(BotiEntitySample.BOTI_PROFILE_NAME, "");
                RemotePlayer player = new RemotePlayer(clientWorld, new GameProfile(profileId, name));
                ValueInput input = TagValueInput.create(
                        ProblemReporter.DISCARDING,
                        clientWorld.registryAccess(),
                        nbt
                );
                player.load(input);
                entity = player;
            } else {
                if (!nbt.contains("id") && typeId != null) {
                    nbt.putString("id", typeId.toString());
                }
                entity = EntityType.loadEntityRecursive(
                        nbt,
                        clientWorld,
                        new EntitySpawnRequest(EntitySpawnReason.LOAD, true),
                        EntityProcessor.NOP
                );
                if (entity == null) {
                    return null;
                }
            }
            entity.setUUID(payload.entityUuid());
            entity.setId(NEXT_GHOST_ENTITY_ID.getAndIncrement());
            return entity;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void snapEntityPose(
            Entity entity,
            float relX,
            float relY,
            float relZ,
            float yaw,
            float pitch,
            float headYaw,
            float bodyYaw
    ) {
        entity.snapTo(relX, relY, relZ, yaw, pitch);
        entity.setDeltaMovement(0.0, 0.0, 0.0);
        if (entity instanceof LivingEntity living) {
            living.setYBodyRot(bodyYaw);
            living.setYHeadRot(headYaw);
            living.yBodyRotO = bodyYaw;
            living.yHeadRotO = headYaw;
        }
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos == null) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = blocksByRel.get(pos);
        return state == null ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getMinY() {
        return BOTTOM_Y;
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return 0xFFFFFF;
    }

    @Override
    public int getBrightness(LightLayer type, BlockPos pos) {
        if (pos == null || chunkLights.isEmpty()) {
            return 15;
        }
        int worldX = footprintOrigin.getX() + pos.getX();
        int worldZ = footprintOrigin.getZ() + pos.getZ();
        PortalLightData light = chunkLights.get(ChunkPos.pack(worldX >> 4, worldZ >> 4));
        return light == null ? 15 : light.brightness(type, pos, 15);
    }

    public record RenderableGhostEntity(Entity entity, LerpedPose pose, float animAgeInTicks, float bobOffset) {
    }

    private static final class GhostEntity {
        final Entity entity;
        final EntityInterpState interp;
        final long animStartMs;
        /** Stable item bob/spin phase; survives duplicate spawn packets that recreate ItemEntity. */
        final float bobOffset;

        GhostEntity(Entity entity, EntityInterpState interp, long animStartMs, float bobOffset) {
            this.entity = entity;
            this.interp = interp;
            this.animStartMs = animStartMs;
            this.bobOffset = bobOffset;
        }
    }
}
