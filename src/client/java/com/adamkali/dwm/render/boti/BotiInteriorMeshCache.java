package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.network.RequestBotiInteriorC2SPayload;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS BOTI placement cache. Prefers synced live snapshots (blocks + BE NBT + entities);
 * falls back to the First Doctor blueprint until a snapshot arrives.
 */
public final class BotiInteriorMeshCache {
    private static final long REQUEST_COOLDOWN_MS = 2000L;
    private static final String PLAYER_ENTITY_ID = "minecraft:player";

    private static final Map<UUID, CachedSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();
    private static Map<BlockPos, BlockState> blueprintFallback = Map.of();

    private BotiInteriorMeshCache() {
    }

    public static boolean hasSnapshot(UUID tardisId) {
        return tardisId != null && SNAPSHOTS.containsKey(tardisId);
    }

    /** Number of synced BE NBT entries for tests / debug. */
    public static int getBlockEntityNbtCount(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        return cached == null ? 0 : cached.blockEntityNbt().size();
    }

    /** Number of synced entity samples for tests / debug. */
    public static int getEntitySampleCount(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        return cached == null ? 0 : cached.entitySamples().size();
    }

    public static Map<BlockPos, BlockState> getVisibleBlocks(UUID tardisId) {
        if (tardisId != null) {
            CachedSnapshot cached = SNAPSHOTS.get(tardisId);
            if (cached != null) {
                return cached.blocks();
            }
            requestIfNeeded(tardisId);
        }
        return blueprintVisibleBlocks();
    }

    public static List<BlockEntity> getBlockEntities(UUID tardisId) {
        if (tardisId == null) {
            return List.of();
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            return List.of();
        }
        if (!cached.renderedBlockEntities().isEmpty()) {
            return cached.renderedBlockEntities();
        }
        if (cached.blockEntityNbt().isEmpty()) {
            return List.of();
        }
        List<BlockEntity> rebuilt = buildBlockEntities(cached.blocks(), cached.blockEntityNbt());
        if (!rebuilt.isEmpty()) {
            SNAPSHOTS.put(tardisId, new CachedSnapshot(
                    cached.revision(),
                    cached.blocks(),
                    cached.blockEntityNbt(),
                    rebuilt,
                    cached.entitySamples(),
                    cached.renderedEntities()
            ));
        }
        return rebuilt;
    }

    public static List<Entity> getEntities(UUID tardisId) {
        if (tardisId == null) {
            return List.of();
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            return List.of();
        }
        if (!cached.renderedEntities().isEmpty()) {
            return cached.renderedEntities();
        }
        if (cached.entitySamples().isEmpty()) {
            return List.of();
        }
        List<Entity> rebuilt = buildEntities(cached.entitySamples());
        if (!rebuilt.isEmpty()) {
            SNAPSHOTS.put(tardisId, new CachedSnapshot(
                    cached.revision(),
                    cached.blocks(),
                    cached.blockEntityNbt(),
                    cached.renderedBlockEntities(),
                    cached.entitySamples(),
                    rebuilt
            ));
        }
        return rebuilt;
    }

    public static void applySnapshot(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        applySnapshot(tardisId, revision, blocks, Map.of(), List.of());
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities
    ) {
        applySnapshot(tardisId, revision, blocks, blockEntities, List.of());
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities
    ) {
        if (tardisId == null || blocks == null || blocks.isEmpty()) {
            return;
        }
        CachedSnapshot existing = SNAPSHOTS.get(tardisId);
        if (existing != null && revision < existing.revision()) {
            return;
        }
        Map<BlockPos, BlockState> blockCopy = Map.copyOf(blocks);
        Map<BlockPos, NbtCompound> beCopy = copyNbtMap(blockEntities);
        List<BotiEntitySample> entityCopy = copyEntitySamples(entities);
        List<BlockEntity> renderedBes = buildBlockEntities(blockCopy, beCopy);
        List<Entity> previousEntities = existing == null ? List.of() : existing.renderedEntities();
        List<Entity> renderedEntities = reconcileEntities(previousEntities, entityCopy);
        SNAPSHOTS.put(tardisId, new CachedSnapshot(
                revision,
                blockCopy,
                beCopy,
                renderedBes,
                entityCopy,
                renderedEntities
        ));
        LAST_REQUEST_MS.remove(tardisId);
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            SNAPSHOTS.remove(tardisId);
            LAST_REQUEST_MS.remove(tardisId);
        }
    }

    public static void invalidateAll() {
        SNAPSHOTS.clear();
        LAST_REQUEST_MS.clear();
        blueprintFallback = Map.of();
    }

    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            UUID tardisId
    ) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        for (Map.Entry<BlockPos, BlockState> entry : getVisibleBlocks(tardisId).entrySet()) {
            BlockPos pos = entry.getKey();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlockAsEntity(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }

        BlockEntityRenderDispatcher beDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
        for (BlockEntity blockEntity : getBlockEntities(tardisId)) {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer =
                    (BlockEntityRenderer<BlockEntity>) beDispatcher.get(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockPos pos = blockEntity.getPos();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }

        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
        World world = client.world;
        if (world != null) {
            entityDispatcher.configure(world, client.gameRenderer.getCamera(), client.player);
        }
        for (Entity entity : getEntities(tardisId)) {
            entityDispatcher.render(
                    entity,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    tickDelta,
                    matrices,
                    vertexConsumers,
                    light
            );
        }
    }

    private static List<BlockEntity> buildBlockEntities(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntityNbt
    ) {
        if (blockEntityNbt == null || blockEntityNbt.isEmpty()) {
            return List.of();
        }
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client == null ? null : client.world;
        if (world == null) {
            return List.of();
        }
        List<BlockEntity> result = new ArrayList<>(blockEntityNbt.size());
        for (Map.Entry<BlockPos, NbtCompound> entry : blockEntityNbt.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = blocks.get(pos);
            if (state == null || entry.getValue() == null) {
                continue;
            }
            BlockEntity be = BlockEntity.createFromNbt(pos, state, entry.getValue(), world.getRegistryManager());
            if (be == null) {
                continue;
            }
            be.setWorld(world);
            result.add(be);
        }
        return List.copyOf(result);
    }

    private static List<Entity> buildEntities(List<BotiEntitySample> samples) {
        return reconcileEntities(List.of(), samples);
    }

    /**
     * Reuses synthetic entities across snapshots (matched by UUID) and only updates pose,
     * avoiding full NBT rebuild every flush which causes living-entity jitter.
     */
    private static List<Entity> reconcileEntities(List<Entity> previous, List<BotiEntitySample> samples) {
        if (samples == null || samples.isEmpty()) {
            return List.of();
        }
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client == null ? null : client.world;
        if (world == null) {
            return List.of();
        }
        Map<UUID, Entity> previousById = new HashMap<>();
        for (Entity entity : previous) {
            if (entity != null) {
                previousById.put(entity.getUuid(), entity);
            }
        }
        List<Entity> result = new ArrayList<>(samples.size());
        for (BotiEntitySample sample : samples) {
            UUID id = sampleEntityUuid(sample);
            Entity existing = id == null ? null : previousById.get(id);
            if (existing != null) {
                updateSyntheticPose(existing, sample);
                result.add(existing);
            } else {
                Entity entity = createSyntheticEntity(world, sample);
                if (entity != null) {
                    result.add(entity);
                }
            }
        }
        return List.copyOf(result);
    }

    private static UUID sampleEntityUuid(BotiEntitySample sample) {
        if (sample == null || sample.nbt() == null) {
            return null;
        }
        NbtCompound nbt = sample.nbt();
        if (nbt.containsUuid("UUID")) {
            return nbt.getUuid("UUID");
        }
        if (nbt.containsUuid(BotiEntitySample.BOTI_PROFILE_ID)) {
            return nbt.getUuid(BotiEntitySample.BOTI_PROFILE_ID);
        }
        return null;
    }

    private static void updateSyntheticPose(Entity entity, BotiEntitySample sample) {
        if (entity instanceof BlockAttachedEntity) {
            // Attachment/facing come from NBT at create time; avoid setPosition clobbering Tile.
            return;
        }
        entity.refreshPositionAndAngles(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch());
        if (entity instanceof LivingEntity living) {
            syncLivingRenderPose(living, sample.yaw());
        }
    }

    /**
     * Synthetic BOTI entities are not ticked, so render-state lerp fields must be snapped with the
     * pose. Otherwise LivingEntityRenderer interpolates forever between stale prev* and new yaw.
     */
    private static void syncLivingRenderPose(LivingEntity living, float yaw) {
        living.setBodyYaw(yaw);
        living.setHeadYaw(yaw);
        living.prevBodyYaw = yaw;
        living.prevHeadYaw = yaw;
        living.limbAnimator.reset();
    }

    private static Entity createSyntheticEntity(World world, BotiEntitySample sample) {
        if (sample == null || sample.nbt() == null) {
            return null;
        }
        try {
            NbtCompound nbt = sample.nbt();
            String id = nbt.getString("id");
            Entity entity;
            if (PLAYER_ENTITY_ID.equals(id)) {
                if (!(world instanceof ClientWorld clientWorld) || !nbt.containsUuid(BotiEntitySample.BOTI_PROFILE_ID)) {
                    return null;
                }
                UUID profileId = nbt.getUuid(BotiEntitySample.BOTI_PROFILE_ID);
                String name = nbt.getString(BotiEntitySample.BOTI_PROFILE_NAME);
                GameProfile profile = new GameProfile(profileId, name == null ? "" : name);
                OtherClientPlayerEntity player = new OtherClientPlayerEntity(clientWorld, profile);
                player.readNbt(nbt);
                entity = player;
            } else {
                Optional<Entity> loaded = EntityType.getEntityFromNbt(nbt, world, SpawnReason.LOAD);
                if (loaded.isEmpty()) {
                    return null;
                }
                entity = loaded.get();
            }
            if (entity instanceof BlockAttachedEntity) {
                // Pos/Tile already plot-relative; refreshPositionAndAngles → setPosition would
                // replace attachedBlockPos with floored entity center.
                entity.setYaw(sample.yaw());
                entity.setPitch(sample.pitch());
                entity.resetPosition();
            } else {
                entity.refreshPositionAndAngles(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch());
                if (entity instanceof LivingEntity living) {
                    syncLivingRenderPose(living, sample.yaw());
                }
            }
            return entity;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Map<BlockPos, NbtCompound> copyNbtMap(Map<BlockPos, NbtCompound> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<BlockPos, NbtCompound> copy = new java.util.HashMap<>(source.size());
        for (Map.Entry<BlockPos, NbtCompound> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return Map.copyOf(copy);
    }

    private static List<BotiEntitySample> copyEntitySamples(List<BotiEntitySample> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<BotiEntitySample> copy = new ArrayList<>(source.size());
        for (BotiEntitySample sample : source) {
            if (sample != null) {
                copy.add(sample);
            }
        }
        return List.copyOf(copy);
    }

    private static void requestIfNeeded(UUID tardisId) {
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
        ClientPlayNetworking.send(new RequestBotiInteriorC2SPayload(tardisId));
    }

    private static Map<BlockPos, BlockState> blueprintVisibleBlocks() {
        if (blueprintFallback.isEmpty()) {
            blueprintFallback = Map.copyOf(FirstDoctorConsoleRoomLayout.botiVisiblePlacements());
        }
        return blueprintFallback;
    }

    private record CachedSnapshot(
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntityNbt,
            List<BlockEntity> renderedBlockEntities,
            List<BotiEntitySample> entitySamples,
            List<Entity> renderedEntities
    ) {
    }
}
