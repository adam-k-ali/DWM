package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.network.RequestSotoGhostC2SPayload;
import com.adamkali.dwm.network.SyncSotoExteriorChunkS2CPayload;
import com.adamkali.dwm.network.SyncSotoExteriorEntitySpawnS2CPayload;
import com.adamkali.dwm.network.SyncSotoExteriorEntityUpdateS2CPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion;
import com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS Phase 1 ghost exterior: streamed chunks + live entities in footprint-relative space.
 * Entity poses are packet-interpolated (no client velocity integration) to avoid snap/jitter.
 */
public final class SotoGhostExterior implements BlockAndTintGetter {
    private static final String PLAYER_ENTITY_ID = "minecraft:player";
    private static final long REQUEST_COOLDOWN_MS = 2000L;
    /** Matches server ghost entity update cadence (every 2 ticks @ 20 TPS). */
    public static final long ENTITY_UPDATE_INTERVAL_MS = 100L;
    private static final int BOTTOM_Y = -64;
    private static final int HEIGHT = 384;

    private static final Map<UUID, SotoGhostExterior> BY_TARDIS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();

    private final UUID tardisId;
    private BlockPos footprintOrigin = BlockPos.ZERO;
    private final Map<Long, Map<BlockPos, BlockState>> chunkBlocks = new ConcurrentHashMap<>();
    private final Map<Long, Map<BlockPos, CompoundTag>> chunkBlockEntities = new ConcurrentHashMap<>();
    private final Map<BlockPos, BlockState> blocksByRel = new ConcurrentHashMap<>();
    private final Map<BlockPos, CompoundTag> blockEntitiesByRel = new ConcurrentHashMap<>();
    private final Map<UUID, GhostEntity> entities = new ConcurrentHashMap<>();

    private SotoGhostExterior(UUID tardisId) {
        this.tardisId = tardisId;
    }

    public static SotoGhostExterior get(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        return BY_TARDIS.get(tardisId);
    }

    public static SotoGhostExterior getOrCreate(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        return BY_TARDIS.computeIfAbsent(tardisId, SotoGhostExterior::new);
    }

    public static boolean hasEntities(UUID tardisId) {
        SotoGhostExterior ghost = get(tardisId);
        return ghost != null && !ghost.entities.isEmpty();
    }

    /**
     * Entities with packet-lerped poses for smooth SOTO rendering.
     */
    public static List<RenderableGhostEntity> getRenderableEntities(UUID tardisId) {
        SotoGhostExterior ghost = get(tardisId);
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

    public static void requestIfNeeded(UUID tardisId) {
        if (tardisId == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = LAST_REQUEST_MS.get(tardisId);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getConnection() == null) {
            return;
        }
        LAST_REQUEST_MS.put(tardisId, now);
        ClientPlayNetworking.send(new RequestSotoGhostC2SPayload(tardisId));
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            BY_TARDIS.remove(tardisId);
            LAST_REQUEST_MS.remove(tardisId);
            SotoGhostMeshCache.invalidate(tardisId);
        }
    }

    public static void invalidateAll() {
        BY_TARDIS.clear();
        LAST_REQUEST_MS.clear();
        SotoGhostMeshCache.invalidateAll();
    }

    public static void clientTick() {
        for (SotoGhostExterior ghost : BY_TARDIS.values()) {
            ghost.tickEntities();
        }
    }

    public static void applyChunk(SyncSotoExteriorChunkS2CPayload payload) {
        if (payload == null || payload.tardisId() == null) {
            return;
        }
        SotoGhostExterior ghost = getOrCreate(payload.tardisId());
        ghost.footprintOrigin = payload.footprintOrigin();
        long key = ChunkPos.asLong(payload.chunkX(), payload.chunkZ());
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
        SotoGhostMeshCache.onChunkApplied(payload.tardisId(), payload.chunkX(), payload.chunkZ(), ghost);
    }

    public static void unloadChunk(UUID tardisId, int chunkX, int chunkZ) {
        SotoGhostExterior ghost = get(tardisId);
        if (ghost == null) {
            return;
        }
        long key = ChunkPos.asLong(chunkX, chunkZ);
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
        SotoGhostMeshCache.onChunkUnloaded(tardisId, chunkX, chunkZ);
    }

    public static void applyEntitySpawn(SyncSotoExteriorEntitySpawnS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.entityUuid() == null) {
            return;
        }
        SotoGhostExterior ghost = getOrCreate(payload.tardisId());
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

    public static void applyEntityUpdate(SyncSotoExteriorEntityUpdateS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.entityUuid() == null) {
            return;
        }
        SotoGhostExterior ghost = get(payload.tardisId());
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
        // Keep entity at the latest sample target; render uses interp between samples.
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

    public static void removeEntity(UUID tardisId, UUID entityUuid) {
        SotoGhostExterior ghost = get(tardisId);
        if (ghost == null || entityUuid == null) {
            return;
        }
        ghost.entities.remove(entityUuid);
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

    private Entity createEntity(SyncSotoExteriorEntitySpawnS2CPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !(client.level instanceof ClientLevel clientWorld)) {
            return null;
        }
        try {
            CompoundTag nbt = payload.nbt() == null ? new CompoundTag() : payload.nbt().copy();
            Identifier typeId = payload.typeId();
            String id = typeId == null ? nbt.getString("id") : typeId.toString();
            Entity entity;
            if (PLAYER_ENTITY_ID.equals(id)) {
                UUID profileId = payload.entityUuid();
                if (nbt.hasUUID(BotiEntitySample.BOTI_PROFILE_ID)) {
                    profileId = nbt.getUUID(BotiEntitySample.BOTI_PROFILE_ID);
                }
                String name = nbt.contains(BotiEntitySample.BOTI_PROFILE_NAME)
                        ? nbt.getString(BotiEntitySample.BOTI_PROFILE_NAME)
                        : "";
                RemotePlayer player = new RemotePlayer(clientWorld, new GameProfile(profileId, name));
                player.load(nbt);
                entity = player;
            } else {
                if (!nbt.contains("id") && typeId != null) {
                    nbt.putString("id", typeId.toString());
                }
                Optional<Entity> loaded = EntityType.create(nbt, clientWorld, EntitySpawnReason.LOAD);
                if (loaded.isEmpty()) {
                    return null;
                }
                entity = loaded.get();
            }
            entity.setUUID(payload.entityUuid());
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
        entity.moveTo(relX, relY, relZ, yaw, pitch);
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
    public float getShade(Direction direction, boolean shaded) {
        return 1.0f;
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
