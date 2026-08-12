package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntitySpawnS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntityUpdateS2CPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion;
import com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.render.portal.PortalFrameCache;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
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

    private final PortalStreamKind kind;
    private final UUID tardisId;
    private BlockPos footprintOrigin = BlockPos.ZERO;
    private final Map<Long, Map<BlockPos, BlockState>> chunkBlocks = new ConcurrentHashMap<>();
    private final Map<Long, Map<BlockPos, CompoundTag>> chunkBlockEntities = new ConcurrentHashMap<>();
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
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null || ghost.entities.isEmpty()) {
            return List.of();
        }
        long now = Util.getMillis();
        List<RenderableGhostEntity> result = new ArrayList<>(ghost.entities.size());
        for (GhostEntity ghostEntity : ghost.entities.values()) {
            if (ghostEntity.entity == null || ghostEntity.interp == null) {
                continue;
            }
            LerpedPose pose = BotiEntityMotion.lerpPose(ghostEntity.interp, now, ENTITY_UPDATE_INTERVAL_MS);
            result.add(new RenderableGhostEntity(ghostEntity.entity, pose));
        }
        return List.copyOf(result);
    }

    public static void invalidate(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        PortalSceneStore.SceneKey key = new PortalSceneStore.SceneKey(kind, tardisId);
        BY_KEY.remove(key);
        SotoGhostMeshCache.invalidate(kind, tardisId);
    }

    public static void invalidateAll() {
        BY_KEY.clear();
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
        Map<BlockPos, BlockState> previousBlocks = ghost.chunkBlocks.put(key, new HashMap<>(payload.toBlockMap()));
        Map<BlockPos, CompoundTag> previousBes = ghost.chunkBlockEntities.put(key, new HashMap<>(payload.toBlockEntityMap()));
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
        ghost.blocksByRel.putAll(payload.toBlockMap());
        ghost.blockEntitiesByRel.putAll(payload.toBlockEntityMap());
        SotoGhostMeshCache.onChunkApplied(kind, payload.tardisId(), payload.chunkX(), payload.chunkZ(), ghost);
    }

    public static void unloadChunk(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null) {
            return;
        }
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<BlockPos, BlockState> removedBlocks = ghost.chunkBlocks.remove(key);
        Map<BlockPos, CompoundTag> removedBes = ghost.chunkBlockEntities.remove(key);
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
    }

    public static void applyEntitySpawn(PortalStreamKind kind, SyncPortalEntitySpawnS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.entityUuid() == null) {
            return;
        }
        SotoGhostExterior ghost = getOrCreate(kind, payload.tardisId());
        Entity entity = ghost.createEntity(payload);
        if (entity == null) {
            return;
        }
        long now = Util.getMillis();
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
        ghost.entities.put(payload.entityUuid(), new GhostEntity(entity, interp));
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
                : previous.interp.advanceTo(
                        payload.relX(), payload.relY(), payload.relZ(), payload.yaw(), payload.pitch(), now);

        Entity entity = previous.entity;
        if (entity instanceof LivingEntity living) {
            float speed = BotiEntityMotion.limbSpeed(next.fromX(), next.fromZ(), next.toX(), next.toZ());
            living.walkAnimation.update(speed, 0.4f, living.isBaby() ? 3.0f : 1.0f);
        }
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
        ghost.entities.put(payload.entityUuid(), new GhostEntity(entity, next));
    }

    public static void removeEntity(PortalStreamKind kind, UUID tardisId, UUID entityUuid) {
        SotoGhostExterior ghost = get(kind, tardisId);
        if (ghost == null || entityUuid == null) {
            return;
        }
        ghost.entities.remove(entityUuid);
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
     */
    private void tickEntities() {
        for (GhostEntity ghostEntity : entities.values()) {
            Entity entity = ghostEntity.entity;
            if (entity == null) {
                continue;
            }
            entity.tickCount++;
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
        return 15;
    }

    public record RenderableGhostEntity(Entity entity, LerpedPose pose) {
    }

    private record GhostEntity(Entity entity, EntityInterpState interp) {
    }
}
