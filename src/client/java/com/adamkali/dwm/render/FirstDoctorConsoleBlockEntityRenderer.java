package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.model.tileentity.BiomeSelectorModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorConsoleModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class FirstDoctorConsoleBlockEntityRenderer implements BlockEntityRenderer<FirstDoctorConsoleBlockEntity> {
    private static final float PX = 1.0f / 16.0f;

    private final FirstDoctorConsoleModel model;
    private final BiomeSelectorModel biomeSelectorModel;

    public FirstDoctorConsoleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new FirstDoctorConsoleModel(
                context.getLayerModelPart(FirstDoctorConsoleModel.LAYER_LOCATION));
        this.biomeSelectorModel = new BiomeSelectorModel(
                context.getLayerModelPart(BiomeSelectorModel.LAYER_LOCATION));
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
        model.setAngles(renderState);
        biomeSelectorModel.setAngles(renderState);

        matrices.push();
        applyTransforms(matrices, facing);
        VertexConsumer consoleVertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(FirstDoctorConsoleModel.TEXTURE_LOCATION));
        model.render(matrices, consoleVertices, light, overlay);

        applyPanel3SelectorTransforms(matrices);
        VertexConsumer selectorVertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(BiomeSelectorModel.TEXTURE_LOCATION));
        biomeSelectorModel.render(matrices, selectorVertices, light, overlay);
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
     * Panel3 → bone9 deck → mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel3SelectorTransforms(MatrixStack matrices) {
        matrices.translate(0.0, FirstDoctorConsoleControls.PANEL_PIVOT_Y_PX * PX, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(FirstDoctorConsoleControls.PANEL3_YAW_RAD));

        matrices.translate(
                0.0,
                FirstDoctorConsoleControls.DECK_PIVOT_Y_PX * PX,
                FirstDoctorConsoleControls.DECK_PIVOT_Z_PX * PX
        );
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(FirstDoctorConsoleControls.DECK_PITCH_RAD));

        matrices.translate(
                FirstDoctorConsoleControls.SELECTOR_MOUNT_X_PX * PX,
                FirstDoctorConsoleControls.SELECTOR_MOUNT_Y_PX * PX,
                FirstDoctorConsoleControls.SELECTOR_MOUNT_Z_PX * PX
        );
        float s = FirstDoctorConsoleControls.SELECTOR_SCALE;
        matrices.scale(s, s, s);
    }

    @Override
    public int getRenderDistance() {
        return 64;
    }
}
