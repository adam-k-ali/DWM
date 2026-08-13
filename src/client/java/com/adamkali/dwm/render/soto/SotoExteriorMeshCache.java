package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-TARDIS SOTO exterior cache for shell metadata and atmosphere.
 * Now delegates meta storage to {@link PortalSceneStore} and terrain/entities
 * to kind-keyed {@link SotoGhostExterior}.
 * <p>
 * Ghost entity / block-entity draws use extract+submit against {@link SubmitNodeCollector}
 * and {@link CameraRenderState}.
 */
public final class SotoExteriorMeshCache {
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;
    private static final AtomicInteger NEXT_FALLBACK_ENTITY_ID = new AtomicInteger(2_000_000);

    private SotoExteriorMeshCache() {
    }

    public static boolean hasSnapshot(UUID tardisId) {
        return tardisId != null && PortalSceneStore.getShell(PortalStreamKind.SOTO, tardisId) != null;
    }

    public static PortalShellState getShellState(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        PortalShellState shell = PortalSceneStore.getShell(PortalStreamKind.SOTO, tardisId);
        if (shell == null) {
            PortalSceneStore.requestIfNeeded(PortalStreamKind.SOTO, tardisId);
        }
        return shell;
    }

    public static SotoAtmosphere getAtmosphere(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.SOTO, tardisId);
        if (portal == null) {
            PortalSceneStore.requestIfNeeded(PortalStreamKind.SOTO, tardisId);
            return null;
        }
        return SotoAtmosphere.fromPortal(portal);
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            PortalSceneStore.invalidate(PortalStreamKind.SOTO, tardisId);
        }
    }

    public static void invalidateAll() {
        PortalSceneStore.invalidateAll();
    }

    /**
     * Submits ghost block entities into {@code submitNodeCollector}. Caller must flush
     * while the portal target is bound.
     *
     * @return number of BEs successfully submitted
     */
    public static int renderGhostBlockEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            int light,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        return renderGhostBlockEntities(
                matrices, submitNodeCollector, cameraState, light, tickDelta,
                PortalStreamKind.SOTO, tardisId, camera
        );
    }

    public static int renderGhostBlockEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            int light,
            float tickDelta,
            PortalStreamKind kind,
            UUID tardisId,
            Camera camera
    ) {
        Minecraft client = Minecraft.getInstance();
        SotoGhostExterior ghost = SotoGhostExterior.get(kind, tardisId);
        if (client == null || ghost == null || matrices == null || submitNodeCollector == null || cameraState == null) {
            return 0;
        }
        BlockEntityRenderDispatcher beDispatcher = client.getBlockEntityRenderDispatcher();
        if (camera != null) {
            beDispatcher.prepare(camera.position());
        }
        List<BlockEntity> bes = ghost.buildRenderedBlockEntities();
        int submitted = 0;
        int lightCoords = light >= 0 ? light : FULLBRIGHT;
        Vec3 cameraPos = cameraState.pos;
        for (BlockEntity blockEntity : bes) {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer =
                    (BlockEntityRenderer<BlockEntity, BlockEntityRenderState>) beDispatcher.getRenderer(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockEntityRenderState state = beDispatcher.tryExtractRenderState(blockEntity, tickDelta, null, false);
            if (state == null) {
                continue;
            }
            state.lightCoords = lightCoords;
            BlockPos pos = blockEntity.getBlockPos();
            matrices.pushPose();
            matrices.translate(
                    pos.getX() - cameraPos.x,
                    pos.getY() - cameraPos.y,
                    pos.getZ() - cameraPos.z
            );
            beDispatcher.submit(state, matrices, submitNodeCollector, cameraState);
            matrices.popPose();
            submitted++;
        }
        return submitted;
    }

    /**
     * Submits ghost entities into {@code submitNodeCollector}. Caller must flush while the
     * portal target is bound.
     *
     * @return number of entities successfully submitted
     */
    public static int renderGhostEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        return renderGhostEntities(
                matrices, submitNodeCollector, cameraState, tickDelta,
                PortalStreamKind.SOTO, tardisId, camera
        );
    }

    public static int renderGhostEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            float tickDelta,
            PortalStreamKind kind,
            UUID tardisId,
            Camera camera
    ) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || matrices == null || submitNodeCollector == null || cameraState == null
                || !SotoGhostExterior.hasEntities(kind, tardisId)) {
            return 0;
        }
        float partialTick = resolvePartialTick(client, tickDelta);
        PortalPerfStats.notePartialTick(partialTick);
        EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
        if (camera != null) {
            entityDispatcher.prepare(camera, client.player);
        }
        List<SotoGhostExterior.RenderableGhostEntity> ghosts =
                SotoGhostExterior.getRenderableEntities(kind, tardisId);
        int submitted = 0;
        Vec3 cameraPos = cameraState.pos;
        for (SotoGhostExterior.RenderableGhostEntity ghost : ghosts) {
            Entity entity = ghost.entity();
            LerpedPose pose = ghost.pose();
            applyLerpedPoseForExtract(entity, pose);
            try {
                ensureGhostEntityId(entity);
                // Fresh END_MAIN partial: age-based anims (item bob/spin) need current partial ticks.
                EntityRenderState entityState = entityDispatcher.extractEntity(entity, partialTick);
                if (entityState == null) {
                    continue;
                }
                if (entity instanceof ItemEntity
                        && entityState instanceof ItemEntityRenderState itemState) {
                    // Wall-clock age + locked bob phase (duplicate spawns recreate ItemEntity with new bobOffs).
                    itemState.ageInTicks = ghost.animAgeInTicks();
                    itemState.bobOffset = ghost.bobOffset();
                    PortalPerfStats.noteItemAgeInTicks(itemState.ageInTicks);
                }
                entityState.lightCoords = FULLBRIGHT;
                entityDispatcher.submit(
                        entityState,
                        cameraState,
                        pose.x() - cameraPos.x,
                        pose.y() - cameraPos.y,
                        pose.z() - cameraPos.z,
                        matrices,
                        submitNodeCollector
                );
                submitted++;
            } catch (RuntimeException ignored) {
            }
        }
        return submitted;
    }

    /**
     * Prefer the live game-time partial tick at FBO draw time; fall back to the scheduled scene value.
     */
    static float resolvePartialTick(Minecraft client, float fallbackTickDelta) {
        if (client == null || client.getDeltaTracker() == null) {
            return fallbackTickDelta;
        }
        return client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    private static void ensureGhostEntityId(Entity entity) {
        try {
            entity.getId();
        } catch (IllegalStateException missingId) {
            entity.setId(NEXT_FALLBACK_ENTITY_ID.getAndIncrement());
        }
    }

    /**
     * True when packet yaw/pitch should drive extract (living locomotion).
     * Items spin/bob from age + partialTick — packet rotation would fight that.
     */
    static boolean shouldApplyPacketRotation(Entity entity) {
        return entity != null && !(entity instanceof ItemEntity);
    }

    /**
     * Snaps the ghost entity to the packet-lerped pose before {@code extractEntity},
     * so baked render state matches submit translation.
     * {@link ItemEntity}: position only (preserve yaw/pitch for age-based spin).
     */
    static void applyLerpedPoseForExtract(Entity entity, LerpedPose pose) {
        if (entity == null || pose == null) {
            return;
        }
        if (shouldApplyPacketRotation(entity)) {
            entity.snapTo(pose.x(), pose.y(), pose.z(), pose.yaw(), pose.pitch());
            entity.setDeltaMovement(0.0, 0.0, 0.0);
            if (entity instanceof LivingEntity living) {
                snapLivingYaw(living, pose.yaw());
            }
        } else {
            entity.snapTo(pose.x(), pose.y(), pose.z(), entity.getYRot(), entity.getXRot());
            entity.setDeltaMovement(0.0, 0.0, 0.0);
        }
    }

    private static void snapLivingYaw(LivingEntity living, float yaw) {
        living.setYBodyRot(yaw);
        living.setYHeadRot(yaw);
        living.yBodyRotO = yaw;
        living.yHeadRotO = yaw;
    }
}
