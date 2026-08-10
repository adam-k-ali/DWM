package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.model.tileentity.BiomeSelectorModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorConsoleModel;
import com.adamkali.dwm.model.tileentity.MaterialisationLeverModel;
import com.adamkali.dwm.model.tileentity.PlanetLocatorModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FirstDoctorConsoleBlockEntityRenderer implements BlockEntityRenderer<FirstDoctorConsoleBlockEntity> {
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
    public void render(
            FirstDoctorConsoleBlockEntity entity,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay
    ) {
        BlockState state = entity.getBlockState();
        Direction facing = state.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

        TardisRenderState renderState = new TardisRenderState();
        TardisTravelPhase phase = TardisLogic.getTravelPhase(entity.getTardisId());
        Level world = entity.getLevel();
        float timeTicks = world == null ? tickDelta : world.getGameTime() + tickDelta;
        renderState.setRotorBobOffset(FirstDoctorConsoleModel.rotorBobOffset(timeTicks, phase.isTraveling()));
        model.setupAnim(renderState);
        biomeSelectorModel.setupAnim(renderState);
        planetLocatorModel.setupAnim(renderState);
        materialisationLeverModel.setupAnim(renderState);

        matrices.pushPose();
        applyTransforms(matrices, facing);
        VertexConsumer consoleVertices = vertexConsumers.getBuffer(
                RenderType.entityTranslucent(FirstDoctorConsoleModel.TEXTURE_LOCATION));
        model.renderToBuffer(matrices, consoleVertices, light, overlay);

        matrices.pushPose();
        applyPanel3BiomeSelectorTransforms(matrices);
        VertexConsumer selectorVertices = vertexConsumers.getBuffer(
                RenderType.entityCutout(BiomeSelectorModel.TEXTURE_LOCATION));
        biomeSelectorModel.renderToBuffer(matrices, selectorVertices, light, overlay);
        matrices.popPose();

        matrices.pushPose();
        applyPanel3PlanetLocatorTransforms(matrices);
        VertexConsumer planetVertices = vertexConsumers.getBuffer(
                RenderType.entityCutout(PlanetLocatorModel.TEXTURE_LOCATION));
        planetLocatorModel.renderToBuffer(matrices, planetVertices, light, overlay);
        matrices.popPose();

        matrices.pushPose();
        applyPanel6LeverTransforms(matrices);
        VertexConsumer leverVertices = vertexConsumers.getBuffer(
                RenderType.entityCutout(MaterialisationLeverModel.TEXTURE_LOCATION));
        materialisationLeverModel.renderToBuffer(matrices, leverVertices, light, overlay);
        matrices.popPose();

        matrices.popPose();
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
