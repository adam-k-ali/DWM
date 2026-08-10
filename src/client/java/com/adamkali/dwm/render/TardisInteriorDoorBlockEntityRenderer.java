package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.render.state.TardisInteriorDoorBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.adamkali.dwm.tardis.interior.TardisSotoGate;
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

    public TardisInteriorDoorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TardisClassicInteriorDoorModel(
                context.bakeLayer(TardisClassicInteriorDoorModel.LAYER_LOCATION));
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
        state.shouldRenderSoto = shouldRenderSoto(state.doorSwing);
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
            // TODO(soto): restore shell → SOTO → doors (depth override) once TardisSotoRenderer
            // accepts SubmitNodeCollector.
            submitSotoPlaceholder(state, poseStack, submitNodeCollector);
        }
        submitFull(animState, poseStack, submitNodeCollector, state);

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
     * Compile-safe SOTO stand-in. Sibling agents own {@code render/soto/**}; re-hook when their
     * submit-era API is ready.
     */
    // TODO(soto): call TardisSotoRenderer with SubmitNodeCollector once available.
    private static void submitSotoPlaceholder(
            TardisInteriorDoorBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        // no-op: MultiBufferSource / immediate flush / depth-func override path removed in 26.2
    }

    /**
     * Portal support lives in the SOTO package (sibling-owned); gate on config + door swing only here.
     */
    private static boolean shouldRenderSoto(float doorSwing) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_SOTO) && TardisSotoGate.shouldShow(doorSwing);
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
