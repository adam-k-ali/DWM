package com.adamkali.dwm.render;

import com.adamkali.dwm.TardisChameleonVariant;
import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SecondDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.TTCapsuleModel;
import com.adamkali.dwm.model.tileentity.TardisModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

import java.util.HashMap;

public class TardisBlockEntityRenderer implements BlockEntityRenderer<TardisBlockEntity> {
    private final HashMap<TardisChameleonVariant, TardisModel> modelCache = new HashMap<>();
    private final HashMap<TardisChameleonVariant, Identifier> textureCache = new HashMap<>();

    private void saveChameleonVariant(TardisChameleonVariant variant, TardisModel model, Identifier texture) {
        modelCache.put(variant, model);
        textureCache.put(variant, texture);
    }

    public TardisBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        saveChameleonVariant(TardisChameleonVariant.TT_CAPSULE, new TTCapsuleModel(context.getLayerModelPart(TTCapsuleModel.LAYER_LOCATION)), TTCapsuleModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.FIRST_DOCTOR_BOX, new FirstDoctorTardisModel(context.getLayerModelPart(FirstDoctorTardisModel.LAYER_LOCATION)), FirstDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.SECOND_DOCTOR_BOX, new SecondDoctorTardisModel(context.getLayerModelPart(SecondDoctorTardisModel.LAYER_LOCATION)), SecondDoctorTardisModel.TEXTURE_LOCATION);
    }

    @Override
    public void render(TardisBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getCachedState();
        int rotation = state.get(TardisBlock.FACING_ROTATION, 0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(textureCache.get(entity.getVariant())));
        this.render(matrices, vertexConsumer, modelCache.get(entity.getVariant()), entity.getDoorState().getDoorSwing(), RotationPropertyHelper.toDegrees(rotation), light, overlay);
    }

    private void render(MatrixStack matrices, VertexConsumer vertices, TardisModel model, float doorProgress, float rotation, int light, int overlay) {
        TardisRenderState state = new TardisRenderState();
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
