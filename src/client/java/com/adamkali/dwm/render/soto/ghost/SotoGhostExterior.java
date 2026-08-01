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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

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
public final class SotoGhostExterior implements BlockRenderView {
    private static final String PLAYER_ENTITY_ID = "minecraft:player";
    private static final long REQUEST_COOLDOWN_MS = 2000L;
    /** Matches server ghost entity update cadence (every 2 ticks @ 20 TPS). */
    public static final long ENTITY_UPDATE_INTERVAL_MS = 100L;
    private static final int BOTTOM_Y = -64;
    private static final int HEIGHT = 384;

    private static final Map<UUID, SotoGhostExterior> BY_TARDIS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();

    private final UUID tardisId;
    private BlockPos footprintOrigin = BlockPos.ORIGIN;
    private final Map<Long, Map<BlockPos, BlockState>> chunkBlocks = new ConcurrentHashMap<>();
    private final Map<Long, Map<BlockPos, NbtCompound>> chunkBlockEntities = new ConcurrentHashMap<>();
    private final Map<BlockPos, BlockState> blocksByRel = new ConcurrentHashMap<>();
    private final Map<BlockPos, NbtCompound> blockEntitiesByRel = new ConcurrentHashMap<>();
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
        long now = Util.getMeasuringTimeMs();
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) {
            return;
        }
        LAST_REQUEST_MS.put(tardisId, now);
        ClientPlayNetworking.send(new RequestSotoGhostC2SPayload(tardisId));
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            BY_TARDIS.remove(tardisId);
            LAST_REQUEST_MS.remove(tardisId);
        }
    }

    public static void invalidateAll() {
        BY_TARDIS.clear();
        LAST_REQUEST_MS.clear();
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
        long key = ChunkPos.toLong(payload.chunkX(), payload.chunkZ());
        Map<BlockPos, BlockState> previousBlocks = ghost.chunkBlocks.put(key, new HashMap<>(payload.toBlockMap()));
        Map<BlockPos, NbtCompound> previousBes = ghost.chunkBlockEntities.put(key, new HashMap<>(payload.toBlockEntityMap()));
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
    }

    public static void unloadChunk(UUID tardisId, int chunkX, int chunkZ) {
        SotoGhostExterior ghost = get(tardisId);
        if (ghost == null) {
            return;
        }
        long key = ChunkPos.toLong(chunkX, chunkZ);
        Map<BlockPos, BlockState> removedBlocks = ghost.chunkBlocks.remove(key);
        Map<BlockPos, NbtCompound> removedBes = ghost.chunkBlockEntities.remove(key);
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
        long now = Util.getMeasuringTimeMs();
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
        long now = Util.getMeasuringTimeMs();
        EntityInterpState next = previous.interp == null
                ? EntityInterpState.identity(
                        payload.relX(), payload.relY(), payload.relZ(), payload.yaw(), payload.pitch(), now)
                : previous.interp.advanceTo(
                        payload.relX(), payload.relY(), payload.relZ(), payload.yaw(), payload.pitch(), now);

        Entity entity = previous.entity;
        if (entity instanceof LivingEntity living) {
            float speed = BotiEntityMotion.limbSpeed(next.fromX(), next.fromZ(), next.toX(), next.toZ());
            living.limbAnimator.updateLimbs(speed, 0.4f, living.isBaby() ? 3.0f : 1.0f);
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

    /**
     * Advances animation age only. Position is packet-driven + render-lerped (no velocity integrate).
     */
    private void tickEntities() {
        for (GhostEntity ghostEntity : entities.values()) {
            Entity entity = ghostEntity.entity;
            if (entity == null) {
                continue;
            }
            entity.age++;
        }
    }

    private Entity createEntity(SyncSotoExteriorEntitySpawnS2CPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !(client.world instanceof ClientWorld clientWorld)) {
            return null;
        }
        try {
            NbtCompound nbt = payload.nbt() == null ? new NbtCompound() : payload.nbt().copy();
            Identifier typeId = payload.typeId();
            String id = typeId == null ? nbt.getString("id") : typeId.toString();
            Entity entity;
            if (PLAYER_ENTITY_ID.equals(id)) {
                UUID profileId = payload.entityUuid();
                if (nbt.containsUuid(BotiEntitySample.BOTI_PROFILE_ID)) {
                    profileId = nbt.getUuid(BotiEntitySample.BOTI_PROFILE_ID);
                }
                String name = nbt.contains(BotiEntitySample.BOTI_PROFILE_NAME)
                        ? nbt.getString(BotiEntitySample.BOTI_PROFILE_NAME)
                        : "";
                OtherClientPlayerEntity player = new OtherClientPlayerEntity(clientWorld, new GameProfile(profileId, name));
                player.readNbt(nbt);
                entity = player;
            } else {
                if (!nbt.contains("id") && typeId != null) {
                    nbt.putString("id", typeId.toString());
                }
                Optional<Entity> loaded = EntityType.getEntityFromNbt(nbt, clientWorld, SpawnReason.LOAD);
                if (loaded.isEmpty()) {
                    return null;
                }
                entity = loaded.get();
            }
            entity.setUuid(payload.entityUuid());
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
        entity.refreshPositionAndAngles(relX, relY, relZ, yaw, pitch);
        entity.setVelocity(0.0, 0.0, 0.0);
        if (entity instanceof LivingEntity living) {
            living.setBodyYaw(bodyYaw);
            living.setHeadYaw(headYaw);
            living.prevBodyYaw = bodyYaw;
            living.prevHeadYaw = headYaw;
        }
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos == null) {
            return Blocks.AIR.getDefaultState();
        }
        BlockState state = blocksByRel.get(pos);
        return state == null ? Blocks.AIR.getDefaultState() : state;
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
    public int getBottomY() {
        return BOTTOM_Y;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1.0f;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return LightingProvider.DEFAULT;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return 0xFFFFFF;
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return 15;
    }

    public record RenderableGhostEntity(Entity entity, LerpedPose pose) {
    }

    private record GhostEntity(Entity entity, EntityInterpState interp) {
    }
}
