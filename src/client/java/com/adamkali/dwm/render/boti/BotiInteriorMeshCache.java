package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.network.RequestBotiInteriorC2SPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS BOTI placement cache. Prefers synced live snapshots (blocks + BE NBT + entities);
 * falls back to the First Doctor blueprint (blocks + synthetic console BE) until a snapshot arrives.
 */
public final class BotiInteriorMeshCache {
    private static final long REQUEST_COOLDOWN_MS = 2000L;
    private static final String PLAYER_ENTITY_ID = "minecraft:player";
    /** Local console position in {@link FirstDoctorConsoleRoomLayout}. */
    private static final BlockPos BLUEPRINT_CONSOLE_POS = new BlockPos(5, 1, 5);

    private static final Map<UUID, CachedSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();
    private static Map<BlockPos, BlockState> blueprintFallback = Map.of();
    private static List<BlockEntity> blueprintBlockEntities;

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
            return blueprintBlockEntities();
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
                    cached.renderedEntities(),
                    cached.entityInterp()
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
        ReconcileResult rebuilt = reconcileEntities(List.of(), cached.entitySamples(), Map.of());
        if (!rebuilt.entities().isEmpty()) {
            SNAPSHOTS.put(tardisId, new CachedSnapshot(
                    cached.revision(),
                    cached.blocks(),
                    cached.blockEntityNbt(),
                    cached.renderedBlockEntities(),
                    cached.entitySamples(),
                    rebuilt.entities(),
                    rebuilt.interp()
            ));
        }
        return rebuilt.entities();
    }

    public static void applySnapshot(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        applySnapshot(tardisId, revision, blocks, Map.of(), List.of());
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntities
    ) {
        applySnapshot(tardisId, revision, blocks, blockEntities, List.of());
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntities,
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
        Map<BlockPos, CompoundTag> beCopy = copyNbtMap(blockEntities);
        List<BotiEntitySample> entityCopy = copyEntitySamples(entities);
        List<BlockEntity> renderedBes = buildBlockEntities(blockCopy, beCopy);
        List<Entity> previousEntities = existing == null ? List.of() : existing.renderedEntities();
        Map<UUID, EntityInterpState> previousInterp = existing == null ? Map.of() : existing.entityInterp();
        ReconcileResult reconciled = reconcileEntities(previousEntities, entityCopy, previousInterp);
        SNAPSHOTS.put(tardisId, new CachedSnapshot(
                revision,
                blockCopy,
                beCopy,
                renderedBes,
                entityCopy,
                reconciled.entities(),
                reconciled.interp()
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
        blueprintBlockEntities = null;
    }

    public static void render(
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            float tickDelta,
            UUID tardisId
    ) {
        BlockRenderDispatcher blockRenderManager = Minecraft.getInstance().getBlockRenderer();
        for (Map.Entry<BlockPos, BlockState> entry : getVisibleBlocks(tardisId).entrySet()) {
            BlockPos pos = entry.getKey();
            matrices.pushPose();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderSingleBlock(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }

        BlockEntityRenderDispatcher beDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        for (BlockEntity blockEntity : getBlockEntities(tardisId)) {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer =
                    (BlockEntityRenderer<BlockEntity>) beDispatcher.getRenderer(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockPos pos = blockEntity.getBlockPos();
            matrices.pushPose();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, light, OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }

        Minecraft client = Minecraft.getInstance();
        EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
        Level world = client.level;
        if (world != null) {
            entityDispatcher.prepare(world, client.gameRenderer.getMainCamera(), client.player);
        }
        List<Entity> entities = getEntities(tardisId);
        CachedSnapshot cached = tardisId == null ? null : SNAPSHOTS.get(tardisId);
        Map<UUID, EntityInterpState> interpMap = cached == null ? Map.of() : cached.entityInterp();
        long now = Util.getMillis();
        for (Entity entity : entities) {
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            EntityInterpState interp = interpMap.get(entity.getUUID());
            if (interp != null) {
                LerpedPose pose = BotiEntityMotion.lerpPose(interp, now);
                x = pose.x();
                y = pose.y();
                z = pose.z();
                entity.setYRot(pose.yaw());
                entity.setXRot(pose.pitch());
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

    private static List<BlockEntity> buildBlockEntities(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntityNbt
    ) {
        if (blockEntityNbt == null || blockEntityNbt.isEmpty()) {
            return List.of();
        }
        Minecraft client = Minecraft.getInstance();
        Level world = client == null ? null : client.level;
        if (world == null) {
            return List.of();
        }
        List<BlockEntity> result = new ArrayList<>(blockEntityNbt.size());
        for (Map.Entry<BlockPos, CompoundTag> entry : blockEntityNbt.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = blocks.get(pos);
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
     * Reuses synthetic entities across snapshots (matched by UUID) and only updates pose,
     * avoiding full NBT rebuild every flush which causes living-entity jitter.
     */
    private static ReconcileResult reconcileEntities(
            List<Entity> previous,
            List<BotiEntitySample> samples,
            Map<UUID, EntityInterpState> previousInterp
    ) {
        if (samples == null || samples.isEmpty()) {
            return new ReconcileResult(List.of(), Map.of());
        }
        Minecraft client = Minecraft.getInstance();
        Level world = client == null ? null : client.level;
        if (world == null) {
            return new ReconcileResult(List.of(), Map.of());
        }
        Map<UUID, Entity> previousById = new HashMap<>();
        for (Entity entity : previous) {
            if (entity != null) {
                previousById.put(entity.getUUID(), entity);
            }
        }
        Map<UUID, EntityInterpState> prevInterp = previousInterp == null ? Map.of() : previousInterp;
        Map<UUID, EntityInterpState> nextInterp = new HashMap<>();
        List<Entity> result = new ArrayList<>(samples.size());
        long now = Util.getMillis();
        for (BotiEntitySample sample : samples) {
            UUID id = sampleEntityUuid(sample);
            Entity existing = id == null ? null : previousById.get(id);
            if (existing != null) {
                updateSyntheticPose(existing, sample, id, prevInterp, nextInterp, now);
                result.add(existing);
            } else {
                Entity entity = createSyntheticEntity(world, sample);
                if (entity != null) {
                    UUID createdId = entity.getUUID();
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
        CompoundTag nbt = sample.nbt();
        if (nbt.hasUUID("UUID")) {
            return nbt.getUUID("UUID");
        }
        if (nbt.hasUUID(BotiEntitySample.BOTI_PROFILE_ID)) {
            return nbt.getUUID(BotiEntitySample.BOTI_PROFILE_ID);
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
            // Attachment/facing come from NBT at create time; avoid setPosition clobbering Tile.
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
            living.walkAnimation.update(speed, 0.4f, living.isBaby() ? 3.0f : 1.0f);
        }
        entity.moveTo(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch());
        if (entity instanceof LivingEntity living) {
            snapLivingYaw(living, sample.yaw());
        }
    }

    /**
     * Snap living yaw lerp fields so LivingEntityRenderer does not add a second interpolation
     * on top of {@link BotiEntityMotion} pre-lerped poses.
     */
    private static void snapLivingYaw(LivingEntity living, float yaw) {
        living.setYBodyRot(yaw);
        living.setYHeadRot(yaw);
        living.yBodyRotO = yaw;
        living.yHeadRotO = yaw;
    }

    private static Entity createSyntheticEntity(Level world, BotiEntitySample sample) {
        if (sample == null || sample.nbt() == null) {
            return null;
        }
        try {
            CompoundTag nbt = sample.nbt();
            String id = nbt.getString("id");
            Entity entity;
            if (PLAYER_ENTITY_ID.equals(id)) {
                if (!(world instanceof ClientLevel clientWorld) || !nbt.hasUUID(BotiEntitySample.BOTI_PROFILE_ID)) {
                    return null;
                }
                UUID profileId = nbt.getUUID(BotiEntitySample.BOTI_PROFILE_ID);
                String name = nbt.getString(BotiEntitySample.BOTI_PROFILE_NAME);
                GameProfile profile = new GameProfile(profileId, name == null ? "" : name);
                RemotePlayer player = new RemotePlayer(clientWorld, profile);
                player.load(nbt);
                entity = player;
            } else {
                Optional<Entity> loaded = EntityType.create(nbt, world, EntitySpawnReason.LOAD);
                if (loaded.isEmpty()) {
                    return null;
                }
                entity = loaded.get();
            }
            if (entity instanceof BlockAttachedEntity) {
                // Pos/Tile already plot-relative; refreshPositionAndAngles → setPosition would
                // replace attachedBlockPos with floored entity center.
                entity.setYRot(sample.yaw());
                entity.setXRot(sample.pitch());
                entity.setOldPosAndRot();
            } else {
                entity.moveTo(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch());
                if (entity instanceof LivingEntity living) {
                    snapLivingYaw(living, sample.yaw());
                }
            }
            return entity;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Map<BlockPos, CompoundTag> copyNbtMap(Map<BlockPos, CompoundTag> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<BlockPos, CompoundTag> copy = new java.util.HashMap<>(source.size());
        for (Map.Entry<BlockPos, CompoundTag> entry : source.entrySet()) {
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
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getConnection() == null) {
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

    /**
     * Synthetic BER targets for the blueprint fallback (First Doctor console at room center).
     */
    private static List<BlockEntity> blueprintBlockEntities() {
        if (blueprintBlockEntities == null) {
            BlockState state = FirstDoctorConsoleRoomLayout.placements().get(BLUEPRINT_CONSOLE_POS);
            if (state != null && state.is(DWMBlocks.FIRST_DOCTOR_CONSOLE)) {
                blueprintBlockEntities = List.of(new FirstDoctorConsoleBlockEntity(BLUEPRINT_CONSOLE_POS, state));
            } else {
                blueprintBlockEntities = List.of();
            }
        }
        return blueprintBlockEntities;
    }

    private record ReconcileResult(List<Entity> entities, Map<UUID, EntityInterpState> interp) {
    }

    private record CachedSnapshot(
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntityNbt,
            List<BlockEntity> renderedBlockEntities,
            List<BotiEntitySample> entitySamples,
            List<Entity> renderedEntities,
            Map<UUID, EntityInterpState> entityInterp
    ) {
        private CachedSnapshot {
            entityInterp = entityInterp == null || entityInterp.isEmpty() ? Map.of() : Map.copyOf(entityInterp);
        }
    }
}
