package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

public class TardisBlockEntityRenderer implements BlockEntityRenderer<TardisBlockEntity> {
    private final TTCapsuleModel ttCapsuleModel;

    public TardisBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.ttCapsuleModel = new TTCapsuleModel(context.getLayerModelPart(TTCapsuleModel.LAYER_LOCATION));
    }

    @Override
    public void render(TardisBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getCachedState();
        int rotation = state.get(TardisBlock.FACING_ROTATION, 0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(TTCapsuleModel.TEXTURE_LOCATION));
        this.render(matrices, vertexConsumer, this.ttCapsuleModel, entity.getDoorState().getDoorSwing(), RotationPropertyHelper.toDegrees(rotation), light, overlay);
    }

    private void render(MatrixStack matrices, VertexConsumer vertices, TTCapsuleModel model, float doorProgress, float rotation, int light, int overlay) {
        TTCapsuleRenderState state = new TTCapsuleRenderState();
        state.setDoorSwingProgress(doorProgress);
        matrices.push();
        matrices.scale(2.0f, 2.0f, 2.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        matrices.translate(0.25D, -1.5D, -0.25D);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation - 180.0f));
        model.setAngles(state);
        model.render(matrices, vertices, light, overlay);
        matrices.pop();
    }
}
