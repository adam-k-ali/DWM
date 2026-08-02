package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.model.tileentity.BiomeSelectorModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorConsoleModel;
import com.adamkali.dwm.model.tileentity.MaterialisationLeverModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class FirstDoctorConsoleBlockEntityRenderer implements BlockEntityRenderer<FirstDoctorConsoleBlockEntity> {
    private static final float PX = 1.0f / 16.0f;

    private final FirstDoctorConsoleModel model;
    private final BiomeSelectorModel biomeSelectorModel;
    private final MaterialisationLeverModel materialisationLeverModel;

    public FirstDoctorConsoleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new FirstDoctorConsoleModel(
                context.getLayerModelPart(FirstDoctorConsoleModel.LAYER_LOCATION));
        this.biomeSelectorModel = new BiomeSelectorModel(
                context.getLayerModelPart(BiomeSelectorModel.LAYER_LOCATION));
        this.materialisationLeverModel = new MaterialisationLeverModel(
                context.getLayerModelPart(MaterialisationLeverModel.LAYER_LOCATION));
    }

    @Override
    public void render(
            FirstDoctorConsoleBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        BlockState state = entity.getCachedState();
        Direction facing = state.get(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

        TardisRenderState renderState = new TardisRenderState();
        TardisTravelPhase phase = TardisLogic.getTravelPhase(entity.getTardisId());
        World world = entity.getWorld();
        float timeTicks = world == null ? tickDelta : world.getTime() + tickDelta;
        renderState.setRotorBobOffset(FirstDoctorConsoleModel.rotorBobOffset(timeTicks, phase.isTraveling()));
        model.setAngles(renderState);
        biomeSelectorModel.setAngles(renderState);
        materialisationLeverModel.setAngles(renderState);

        matrices.push();
        applyTransforms(matrices, facing);
        VertexConsumer consoleVertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucent(FirstDoctorConsoleModel.TEXTURE_LOCATION));
        model.render(matrices, consoleVertices, light, overlay);

        matrices.push();
        applyPanel3SelectorTransforms(matrices);
        VertexConsumer selectorVertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(BiomeSelectorModel.TEXTURE_LOCATION));
        biomeSelectorModel.render(matrices, selectorVertices, light, overlay);
        matrices.pop();

        matrices.push();
        applyPanel6LeverTransforms(matrices);
        VertexConsumer leverVertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(MaterialisationLeverModel.TEXTURE_LOCATION));
        materialisationLeverModel.render(matrices, leverVertices, light, overlay);
        matrices.pop();

        matrices.pop();
    }

    /**
     * Console model keeps Blockbench Y-up cuboids (unlike Java-entity door exports),
     * so no X-180 flip — only center on the block, scale, and yaw for facing.
     */
    static void applyTransforms(MatrixStack matrices, Direction facing) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(1.0f, 0.8f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-Direction.getHorizontalDegreesOrThrow(facing)));
    }

    /**
     * Panel3 → deck → mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel3SelectorTransforms(MatrixStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE
        );
    }

    /**
     * Panel6 → deck → mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel6LeverTransforms(MatrixStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                FirstDoctorConsoleControls.LEVER_SCALE
        );
    }

    private static void applyPanelControlTransforms(MatrixStack matrices, float panelYawRad, float scale) {
        matrices.translate(0.0, FirstDoctorConsoleControls.PANEL_PIVOT_Y_PX * PX, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(panelYawRad));

        matrices.translate(
                0.0,
                FirstDoctorConsoleControls.DECK_PIVOT_Y_PX * PX,
                FirstDoctorConsoleControls.DECK_PIVOT_Z_PX * PX
        );
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(FirstDoctorConsoleControls.DECK_PITCH_RAD));

        matrices.translate(
                FirstDoctorConsoleControls.CONTROL_MOUNT_X_PX * PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX * PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX * PX
        );
        matrices.scale(scale, scale, scale);
    }

    @Override
    public int getRenderDistance() {
        return 64;
    }
}
