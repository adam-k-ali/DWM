package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.network.RequestSotoExteriorC2SPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion;
import com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
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
import net.minecraft.util.Util;
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
 * Per-TARDIS SOTO exterior cache. Prefers synced live snapshots (blocks + BE NBT + entities + shell).
 */
public final class SotoExteriorMeshCache {
    private static final long REQUEST_COOLDOWN_MS = 2000L;
    private static final String PLAYER_ENTITY_ID = "minecraft:player";

    private static final Map<UUID, CachedSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_REQUESTED_VIEW_DISTANCE = new ConcurrentHashMap<>();

    private SotoExteriorMeshCache() {
    }

    public record ShellState(
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation
    ) {
    }

    public static int clientViewDistanceChunks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return SotoExteriorSampler.DEFAULT_RADIUS_CHUNKS;
        }
        return SotoExteriorSampler.clampRadiusChunks(client.options.getViewDistance().getValue());
    }

    public static boolean hasSnapshot(UUID tardisId) {
        return tardisId != null && SNAPSHOTS.containsKey(tardisId);
    }

    public static ShellState getShellState(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            requestIfNeeded(tardisId);
            return null;
        }
        return cached.shell();
    }

    public static int getBlockEntityNbtCount(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        return cached == null ? 0 : cached.blockEntityNbt().size();
    }

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
        return Map.of();
    }

    public static List<BlockEntity> getBlockEntities(UUID tardisId) {
        if (tardisId == null) {
            return List.of();
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            requestIfNeeded(tardisId);
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
            SNAPSHOTS.put(tardisId, cached.withRenderedBlockEntities(rebuilt));
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
        ReconcileResult rebuilt = reconcileEntities(List.of(), cached.entitySamples(), Map.of());
        if (!rebuilt.entities().isEmpty()) {
            SNAPSHOTS.put(tardisId, cached.withEntities(rebuilt.entities(), rebuilt.interp()));
        }
        return rebuilt.entities();
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities,
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation
    ) {
        applySnapshot(
                tardisId,
                revision,
                SotoExteriorSampler.DEFAULT_RADIUS_CHUNKS,
                blocks,
                blockEntities,
                entities,
                variant,
                doorSwing,
                isOpen,
                exteriorRotation
        );
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            int radiusChunks,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities,
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation
    ) {
        if (tardisId == null) {
            return;
        }
        CachedSnapshot existing = SNAPSHOTS.get(tardisId);
        if (existing != null && revision < existing.revision()) {
            return;
        }
        int clampedRadius = SotoExteriorSampler.clampRadiusChunks(radiusChunks);
        Map<BlockPos, BlockState> blockCopy = blocks == null ? Map.of() : Map.copyOf(blocks);
        Map<BlockPos, NbtCompound> beCopy = copyNbtMap(blockEntities);
        List<BotiEntitySample> entityCopy = copyEntitySamples(entities);
        List<BlockEntity> renderedBes = buildBlockEntities(blockCopy, beCopy);
        List<Entity> previousEntities = existing == null ? List.of() : existing.renderedEntities();
        Map<UUID, EntityInterpState> previousInterp = existing == null ? Map.of() : existing.entityInterp();
        ReconcileResult reconciled = reconcileEntities(previousEntities, entityCopy, previousInterp);
        ShellState shell = new ShellState(
                variant == null ? TardisChameleonVariant.TT_CAPSULE : variant,
                doorSwing,
                isOpen,
                exteriorRotation
        );
        SotoBakedTerrainMesh terrainMesh = SotoBakedTerrainMesh.bake(blockCopy, LightmapTextureManager.pack(15, 15));
        SotoBakedTerrainMesh previousMesh = existing == null ? null : existing.terrainMesh();
        SNAPSHOTS.put(tardisId, new CachedSnapshot(
                revision,
                clampedRadius,
                blockCopy,
                beCopy,
                renderedBes,
                entityCopy,
                reconciled.entities(),
                reconciled.interp(),
                shell,
                terrainMesh
        ));
        if (previousMesh != null && previousMesh != terrainMesh) {
            previousMesh.close();
        }
        LAST_REQUEST_MS.remove(tardisId);
        LAST_REQUESTED_VIEW_DISTANCE.put(tardisId, clampedRadius);
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            CachedSnapshot removed = SNAPSHOTS.remove(tardisId);
            if (removed != null && removed.terrainMesh() != null) {
                removed.terrainMesh().close();
            }
            LAST_REQUEST_MS.remove(tardisId);
            LAST_REQUESTED_VIEW_DISTANCE.remove(tardisId);
        }
    }

    public static void invalidateAll() {
        for (CachedSnapshot cached : SNAPSHOTS.values()) {
            if (cached.terrainMesh() != null) {
                cached.terrainMesh().close();
            }
        }
        SNAPSHOTS.clear();
        LAST_REQUEST_MS.clear();
        LAST_REQUESTED_VIEW_DISTANCE.clear();
    }

    public static boolean hasBakedTerrainMesh(UUID tardisId) {
        if (tardisId == null) {
            return false;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        return cached != null && cached.terrainMesh() != null && !cached.terrainMesh().isEmpty();
    }

    public static int getBakedTerrainLayerCount(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        return cached == null || cached.terrainMesh() == null ? 0 : cached.terrainMesh().layerCount();
    }

    public static void renderWorld(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            UUID tardisId
    ) {
        maybeRefreshViewDistance(tardisId);
        CachedSnapshot cached = tardisId == null ? null : SNAPSHOTS.get(tardisId);
        int radiusBlocks = SotoExteriorSampler.radiusBlocks(
                cached == null ? clientViewDistanceChunks() : cached.radiusChunks()
        );

        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        SotoBakedTerrainMesh terrainMesh = cached == null ? null : cached.terrainMesh();
        if (terrainMesh != null && !terrainMesh.isEmpty()) {
            if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
                immediate.draw();
            }
            terrainMesh.draw(matrices);
        } else {
            for (Map.Entry<BlockPos, BlockState> entry : getVisibleBlocks(tardisId).entrySet()) {
                BlockPos pos = entry.getKey();
                if (!SotoExteriorSampler.isWithinChebyshev(pos, radiusBlocks)) {
                    continue;
                }
                matrices.push();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderManager.renderBlockAsEntity(
                        entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV
                );
                matrices.pop();
            }
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
            if (!SotoExteriorSampler.isWithinChebyshev(pos, radiusBlocks)) {
                continue;
            }
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
        List<Entity> entities = getEntities(tardisId);
        Map<UUID, EntityInterpState> interpMap = cached == null ? Map.of() : cached.entityInterp();
        long now = Util.getMeasuringTimeMs();
        for (Entity entity : entities) {
            BlockPos entityBlock = BlockPos.ofFloored(entity.getX(), entity.getY(), entity.getZ());
            if (!SotoExteriorSampler.isWithinChebyshev(entityBlock, radiusBlocks)) {
                continue;
            }
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            EntityInterpState interp = interpMap.get(entity.getUuid());
            if (interp != null) {
                LerpedPose pose = BotiEntityMotion.lerpPose(interp, now);
                x = pose.x();
                y = pose.y();
                z = pose.z();
                entity.setYaw(pose.yaw());
                entity.setPitch(pose.pitch());
                if (entity instanceof LivingEntity living) {
                    snapLivingYaw(living, pose.yaw());
                }
            }
            entityDispatcher.render(
                    entity,
                    x,
                    y,
                    z,
                    tickDelta,
                    matrices,
                    vertexConsumers,
                    light
            );
        }
    }

    public static void renderShell(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float ignoredTickDelta,
            UUID tardisId,
            SotoShellModels shellModels
    ) {
        ShellState shell = getShellState(tardisId);
        if (shell == null || shellModels == null) {
            return;
        }
        BlockPos tardisRel = SotoExteriorSampler.RELATIVE_TARDIS_POS;
        matrices.push();
        matrices.translate(tardisRel.getX(), tardisRel.getY(), tardisRel.getZ());
        shellModels.render(
                matrices,
                vertexConsumers,
                light,
                OverlayTexture.DEFAULT_UV,
                shell.variant(),
                shell.doorSwing(),
                shell.exteriorRotation()
        );
        matrices.pop();
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

    private static ReconcileResult reconcileEntities(
            List<Entity> previous,
            List<BotiEntitySample> samples,
            Map<UUID, EntityInterpState> previousInterp
    ) {
        if (samples == null || samples.isEmpty()) {
            return new ReconcileResult(List.of(), Map.of());
        }
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client == null ? null : client.world;
        if (world == null) {
            return new ReconcileResult(List.of(), Map.of());
        }
        Map<UUID, Entity> previousById = new HashMap<>();
        for (Entity entity : previous) {
            if (entity != null) {
                previousById.put(entity.getUuid(), entity);
            }
        }
        Map<UUID, EntityInterpState> prevInterp = previousInterp == null ? Map.of() : previousInterp;
        Map<UUID, EntityInterpState> nextInterp = new HashMap<>();
        List<Entity> result = new ArrayList<>(samples.size());
        long now = Util.getMeasuringTimeMs();
        for (BotiEntitySample sample : samples) {
            UUID id = sampleEntityUuid(sample);
            Entity existing = id == null ? null : previousById.get(id);
            if (existing != null) {
                updateSyntheticPose(existing, sample, id, prevInterp, nextInterp, now);
                result.add(existing);
            } else {
                Entity entity = createSyntheticEntity(world, sample);
                if (entity != null) {
                    UUID createdId = entity.getUuid();
                    if (createdId != null && !(entity instanceof BlockAttachedEntity)) {
                        nextInterp.put(createdId, EntityInterpState.identity(sample, now));
                    }
                    result.add(entity);
                }
            }
        }
        return new ReconcileResult(List.copyOf(result), Map.copyOf(nextInterp));
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

    private static void updateSyntheticPose(
            Entity entity,
            BotiEntitySample sample,
            UUID id,
            Map<UUID, EntityInterpState> previousInterp,
            Map<UUID, EntityInterpState> nextInterp,
            long now
    ) {
        if (entity instanceof BlockAttachedEntity) {
            return;
        }
        EntityInterpState previous = id == null ? null : previousInterp.get(id);
        EntityInterpState next = previous == null
                ? EntityInterpState.identity(sample, now)
                : previous.advanceTo(sample, now);
        if (id != null) {
            nextInterp.put(id, next);
        }
        if (previous != null && entity instanceof LivingEntity living) {
            float speed = BotiEntityMotion.limbSpeed(next.fromX(), next.fromZ(), next.toX(), next.toZ());
            living.limbAnimator.updateLimbs(speed, 0.4f, living.isBaby() ? 3.0f : 1.0f);
        }
        entity.refreshPositionAndAngles(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch());
        if (entity instanceof LivingEntity living) {
            snapLivingYaw(living, sample.yaw());
        }
    }

    private static void snapLivingYaw(LivingEntity living, float yaw) {
        living.setBodyYaw(yaw);
        living.setHeadYaw(yaw);
        living.prevBodyYaw = yaw;
        living.prevHeadYaw = yaw;
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
                entity.setYaw(sample.yaw());
                entity.setPitch(sample.pitch());
                entity.resetPosition();
            } else {
                entity.refreshPositionAndAngles(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch());
                if (entity instanceof LivingEntity living) {
                    snapLivingYaw(living, sample.yaw());
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
        Map<BlockPos, NbtCompound> copy = new HashMap<>(source.size());
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

    private static void maybeRefreshViewDistance(UUID tardisId) {
        if (tardisId == null) {
            return;
        }
        int current = clientViewDistanceChunks();
        Integer last = LAST_REQUESTED_VIEW_DISTANCE.get(tardisId);
        if (last != null && last == current && SNAPSHOTS.containsKey(tardisId)) {
            return;
        }
        requestIfNeeded(tardisId, true);
    }

    private static void requestIfNeeded(UUID tardisId) {
        requestIfNeeded(tardisId, false);
    }

    private static void requestIfNeeded(UUID tardisId, boolean force) {
        long now = System.currentTimeMillis();
        Long last = LAST_REQUEST_MS.get(tardisId);
        if (!force && last != null && now - last < REQUEST_COOLDOWN_MS) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) {
            return;
        }
        int viewDistance = clientViewDistanceChunks();
        LAST_REQUEST_MS.put(tardisId, now);
        LAST_REQUESTED_VIEW_DISTANCE.put(tardisId, viewDistance);
        ClientPlayNetworking.send(new RequestSotoExteriorC2SPayload(tardisId, viewDistance));
    }

    private record ReconcileResult(List<Entity> entities, Map<UUID, EntityInterpState> interp) {
    }

    private record CachedSnapshot(
            int revision,
            int radiusChunks,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntityNbt,
            List<BlockEntity> renderedBlockEntities,
            List<BotiEntitySample> entitySamples,
            List<Entity> renderedEntities,
            Map<UUID, EntityInterpState> entityInterp,
            ShellState shell,
            SotoBakedTerrainMesh terrainMesh
    ) {
        private CachedSnapshot {
            radiusChunks = SotoExteriorSampler.clampRadiusChunks(radiusChunks);
            entityInterp = entityInterp == null || entityInterp.isEmpty() ? Map.of() : Map.copyOf(entityInterp);
            terrainMesh = terrainMesh == null ? SotoBakedTerrainMesh.EMPTY : terrainMesh;
        }

        private CachedSnapshot withRenderedBlockEntities(List<BlockEntity> bes) {
            return new CachedSnapshot(
                    revision, radiusChunks, blocks, blockEntityNbt, bes, entitySamples, renderedEntities,
                    entityInterp, shell, terrainMesh
            );
        }

        private CachedSnapshot withEntities(List<Entity> entities, Map<UUID, EntityInterpState> interp) {
            return new CachedSnapshot(
                    revision, radiusChunks, blocks, blockEntityNbt, renderedBlockEntities, entitySamples,
                    entities, interp, shell, terrainMesh
            );
        }
    }
}
