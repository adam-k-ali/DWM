package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
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
    private final FirstDoctorConsoleModel model;

    public FirstDoctorConsoleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new FirstDoctorConsoleModel(
                context.getLayerModelPart(FirstDoctorConsoleModel.LAYER_LOCATION));
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

        model.setAngles(new TardisRenderState());

        VertexConsumer vertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(FirstDoctorConsoleModel.TEXTURE_LOCATION));

        matrices.push();
        applyTransforms(matrices, facing);
        model.render(matrices, vertices, light, overlay);
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

    @Override
    public int getRenderDistance() {
        return 64;
    }
}
