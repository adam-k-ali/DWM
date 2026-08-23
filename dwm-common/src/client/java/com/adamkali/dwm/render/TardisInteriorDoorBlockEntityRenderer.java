package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.adamkali.dwm.render.soto.TardisSotoRenderer;
import com.adamkali.dwm.render.state.TardisInteriorDoorBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TardisInteriorDoorBlockEntityRenderer
        implements BlockEntityRenderer<TardisInteriorDoorBlockEntity, TardisInteriorDoorBlockEntityRenderState> {
    private final TardisClassicInteriorDoorModel model;
    private final TardisSotoRenderer sotoRenderer;

    public TardisInteriorDoorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TardisClassicInteriorDoorModel(
                context.bakeLayer(TardisClassicInteriorDoorModel.LAYER_LOCATION));
        this.sotoRenderer = new TardisSotoRenderer();
    }

    @Override
    public TardisInteriorDoorBlockEntityRenderState createRenderState() {
        return new TardisInteriorDoorBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            TardisInteriorDoorBlockEntity entity,
            TardisInteriorDoorBlockEntityRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = entity.getBlockState();
        state.facing = blockState.getValueOrElse(TardisInteriorDoorBlock.FACING, Direction.NORTH);
        state.doorSwing = entity.getDoorSwing();
        state.partialTicks = partialTicks;
        state.tardisId = entity.getTardisId();
        state.shouldRenderSoto = TardisSotoRenderer.shouldRender(state.doorSwing);
    }

    @Override
    public void submit(
            TardisInteriorDoorBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        TardisRenderState animState = new TardisRenderState();
        animState.setDoorSwingProgress(state.doorSwing);

        poseStack.pushPose();
        applyTransforms(poseStack, state.facing);

        if (state.shouldRenderSoto) {
            // Shell → SOTO → doors so frames establish depth before the aperture clear,
            // and swung leaves still composite over the exterior preview.
            submitShell(animState, poseStack, submitNodeCollector, state);

            sotoRenderer.render(
                    poseStack,
                    submitNodeCollector,
                    state.partialTicks,
                    state.tardisId,
                    state.blockPos,
                    state.facing
            );

            submitDoors(animState, poseStack, submitNodeCollector, state);
        } else {
            submitFull(animState, poseStack, submitNodeCollector, state);
        }

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        // Exterior preview extends several blocks beyond the door aperture.
        return 128;
    }

    private void submitFull(
            TardisRenderState animState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            TardisInteriorDoorBlockEntityRenderState state
    ) {
        submitNodeCollector.submitModel(
                model,
                animState,
                poseStack,
                RenderTypes.entityCutout(TardisClassicInteriorDoorModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
    }

    /**
     * Shell without door leaves. Uses {@link SubmitNodeCollector#submitCustomGeometry} so door
     * visibility can be toggled at flush time (deferred {@code submitModel} would see restored parts).
     */
    private void submitShell(
            TardisRenderState animState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            TardisInteriorDoorBlockEntityRenderState state
    ) {
        int light = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityCutout(TardisClassicInteriorDoorModel.TEXTURE_LOCATION),
                (pose, consumer) -> {
                    PoseStack local = new PoseStack();
                    local.last().set(pose);
                    model.setupAnim(animState);
                    model.renderShell(local, consumer, light, OverlayTexture.NO_OVERLAY);
                });
    }

    /**
     * Door leaves only, drawn after SOTO so open doors sit over the exterior preview.
     */
    private void submitDoors(
            TardisRenderState animState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            TardisInteriorDoorBlockEntityRenderState state
    ) {
        int light = state.lightCoords;
        submitNodeCollector.order(PortalDoorRenderer.DOOR_OVERLAY_ORDER).submitCustomGeometry(
                poseStack,
                PortalDoorRenderer.doorOverlayRenderType(TardisClassicInteriorDoorModel.TEXTURE_LOCATION),
                (pose, consumer) -> {
                    PoseStack local = new PoseStack();
                    local.last().set(pose);
                    model.setupAnim(animState);
                    model.renderDoors(local, consumer, light, OverlayTexture.NO_OVERLAY);
                });
    }

    /**
     * Standard Blockbench tile-entity placement: pivot at top of door volume, X-180 flip,
     * then yaw for {@code facing}. Model pixel units render as 1/16 block.
     */
    static void applyTransforms(PoseStack matrices, Direction facing) {
        matrices.translate(0.5, TardisInteriorDoorShapes.MODEL_HEIGHT_BLOCKS, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(-Direction.getYRot(facing)));
        // Center the ~3-block-wide mesh on the 3-wide bank (origin is bank start cell).
        matrices.translate(TardisInteriorDoorShapes.BANK_CENTER_OFFSET_BLOCKS, 0.0, 0.0);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0f));
        matrices.translate(-TardisInteriorDoorShapes.MODEL_CENTER_X_PX / 16.0F, 0.0F, 0.0F);
    }
}
