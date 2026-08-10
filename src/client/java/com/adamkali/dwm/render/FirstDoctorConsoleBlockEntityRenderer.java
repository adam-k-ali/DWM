package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.model.tileentity.BiomeSelectorModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorConsoleModel;
import com.adamkali.dwm.model.tileentity.MaterialisationLeverModel;
import com.adamkali.dwm.model.tileentity.PlanetLocatorModel;
import com.adamkali.dwm.render.state.FirstDoctorConsoleBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisLogic;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FirstDoctorConsoleBlockEntityRenderer
        implements BlockEntityRenderer<FirstDoctorConsoleBlockEntity, FirstDoctorConsoleBlockEntityRenderState> {
    private static final float PX = 1.0f / 16.0f;

    private final FirstDoctorConsoleModel model;
    private final BiomeSelectorModel biomeSelectorModel;
    private final PlanetLocatorModel planetLocatorModel;
    private final MaterialisationLeverModel materialisationLeverModel;

    public FirstDoctorConsoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new FirstDoctorConsoleModel(
                context.bakeLayer(FirstDoctorConsoleModel.LAYER_LOCATION));
        this.biomeSelectorModel = new BiomeSelectorModel(
                context.bakeLayer(BiomeSelectorModel.LAYER_LOCATION));
        this.planetLocatorModel = new PlanetLocatorModel(
                context.bakeLayer(PlanetLocatorModel.LAYER_LOCATION));
        this.materialisationLeverModel = new MaterialisationLeverModel(
                context.bakeLayer(MaterialisationLeverModel.LAYER_LOCATION));
    }

    @Override
    public FirstDoctorConsoleBlockEntityRenderState createRenderState() {
        return new FirstDoctorConsoleBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            FirstDoctorConsoleBlockEntity entity,
            FirstDoctorConsoleBlockEntityRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = entity.getBlockState();
        state.facing = blockState.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

        TardisTravelPhase phase = TardisLogic.getTravelPhase(entity.getTardisId());
        Level world = entity.getLevel();
        float timeTicks = world == null ? partialTicks : world.getGameTime() + partialTicks;
        state.rotorBobOffset = FirstDoctorConsoleModel.rotorBobOffset(timeTicks, phase.isTraveling());
    }

    @Override
    public void submit(
            FirstDoctorConsoleBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        TardisRenderState animState = new TardisRenderState();
        animState.setRotorBobOffset(state.rotorBobOffset);

        poseStack.pushPose();
        applyTransforms(poseStack, state.facing);

        submitNodeCollector.submitModel(
                model,
                animState,
                poseStack,
                RenderTypes.entityTranslucent(FirstDoctorConsoleModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);

        poseStack.pushPose();
        applyPanel3BiomeSelectorTransforms(poseStack);
        submitNodeCollector.submitModel(
                biomeSelectorModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(BiomeSelectorModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        poseStack.pushPose();
        applyPanel3PlanetLocatorTransforms(poseStack);
        submitNodeCollector.submitModel(
                planetLocatorModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(PlanetLocatorModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        poseStack.pushPose();
        applyPanel6LeverTransforms(poseStack);
        submitNodeCollector.submitModel(
                materialisationLeverModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(MaterialisationLeverModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        poseStack.popPose();
    }

    /**
     * Console model keeps Blockbench Y-up cuboids (unlike Java-entity door exports),
     * so no X-180 flip — only center on the block, scale, and yaw for facing.
     */
    static void applyTransforms(PoseStack matrices, Direction facing) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(1.0f, 0.8f, 1.0f);
        matrices.mulPose(Axis.YP.rotationDegrees(-Direction.getYRot(facing)));
    }

    /**
     * Panel3 → deck → biome mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel3BiomeSelectorTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE,
                FirstDoctorConsoleControls.BIOME_SELECTOR_MOUNT_X_PX
        );
    }

    /**
     * Panel3 → deck → planet locator mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel3PlanetLocatorTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE,
                FirstDoctorConsoleControls.PLANET_LOCATOR_MOUNT_X_PX
        );
    }

    /**
     * Panel6 → deck → mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel6LeverTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                FirstDoctorConsoleControls.LEVER_SCALE,
                FirstDoctorConsoleControls.LEVER_MOUNT_X_PX
        );
    }

    private static void applyPanelControlTransforms(
            PoseStack matrices,
            float panelYawRad,
            float scale,
            float mountXPx
    ) {
        matrices.translate(0.0, FirstDoctorConsoleControls.PANEL_PIVOT_Y_PX * PX, 0.0);
        matrices.mulPose(Axis.YP.rotation(panelYawRad));

        matrices.translate(
                0.0,
                FirstDoctorConsoleControls.DECK_PIVOT_Y_PX * PX,
                FirstDoctorConsoleControls.DECK_PIVOT_Z_PX * PX
        );
        matrices.mulPose(Axis.XP.rotation(FirstDoctorConsoleControls.DECK_PITCH_RAD));

        matrices.translate(
                mountXPx * PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX * PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX * PX
        );
        matrices.scale(scale, scale, scale);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
