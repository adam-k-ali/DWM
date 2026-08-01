package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorRenderAnchor;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class TardisInteriorDoorBlockEntityRenderer implements BlockEntityRenderer<TardisInteriorDoorBlockEntity> {
    /**
     * Model spans roughly X=-2..24 (center ≈ 11). Primary cell is the bank's west/start cell;
     * shift so the double door centers on a 3-wide bank (+1 block along the wall).
     */
    private static final float MODEL_CENTER_X_PX = 11.0F;
    /** Door mesh is 32px tall (~2 blocks); pivot sits at the top after the Blockbench X-180 flip. */
    private static final float MODEL_HEIGHT_BLOCKS = 2.0F;
    private static final float BANK_CENTER_OFFSET_BLOCKS = 1.0F;

    private final TardisClassicInteriorDoorModel model;

    public TardisInteriorDoorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new TardisClassicInteriorDoorModel(
                context.getLayerModelPart(TardisClassicInteriorDoorModel.LAYER_LOCATION));
    }

    @Override
    public void render(
            TardisInteriorDoorBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        BlockState state = entity.getCachedState();
        Direction facing = state.get(TardisInteriorDoorBlock.FACING, Direction.NORTH);
        if (entity.getWorld() == null
                || !TardisInteriorDoorRenderAnchor.isPrimary(entity.getWorld(), entity.getPos(), facing)) {
            return;
        }

        TardisRenderState renderState = new TardisRenderState();
        renderState.setDoorSwingProgress(entity.getDoorSwing());
        model.setAngles(renderState);

        VertexConsumer vertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(TardisClassicInteriorDoorModel.TEXTURE_LOCATION));

        matrices.push();
        applyTransforms(matrices, facing);
        model.render(matrices, vertices, light, overlay);
        matrices.pop();
    }

    /**
     * Standard Blockbench tile-entity placement: pivot at top of door volume, X-180 flip,
     * then yaw for {@code facing}. Model pixel units render as 1/16 block.
     */
    static void applyTransforms(MatrixStack matrices, Direction facing) {
        matrices.translate(0.5, MODEL_HEIGHT_BLOCKS, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-Direction.getHorizontalDegreesOrThrow(facing)));
        // Center the ~1.6-block-wide mesh on the 3-wide bank (primary is bank start cell).
        matrices.translate(BANK_CENTER_OFFSET_BLOCKS, 0.0, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        matrices.translate(-MODEL_CENTER_X_PX / 16.0F, 0.0F, 0.0F);
    }
}
