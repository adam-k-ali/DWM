package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.model.tileentity.*;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.logic.TardisLogic;
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
import java.util.Objects;

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
        saveChameleonVariant(TardisChameleonVariant.THIRD_DOCTOR_BOX, new ThirdDoctorTardisModel(context.getLayerModelPart(ThirdDoctorTardisModel.LAYER_LOCATION)), ThirdDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.FOURTH_DOCTOR_BOX, new FourthDoctorTardisModel(context.getLayerModelPart(FourthDoctorTardisModel.LAYER_LOCATION)), FourthDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.FIFTH_DOCTOR_BOX, new FifthDoctorTardisModel(context.getLayerModelPart(FifthDoctorTardisModel.LAYER_LOCATION)), FifthDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.SIXTH_DOCTOR_BOX, new SixthDoctorTardisModel(context.getLayerModelPart(SixthDoctorTardisModel.LAYER_LOCATION)), SixthDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.SEVENTH_DOCTOR_BOX, new SeventhDoctorTardisModel(context.getLayerModelPart(SeventhDoctorTardisModel.LAYER_LOCATION)), SeventhDoctorTardisModel.TEXTURE_LOCATION);
    }

    @Override
    public void render(TardisBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getCachedState();
        int rotation = state.get(TardisBlock.FACING_ROTATION, 0);

        TardisChameleonVariant variant = Objects.requireNonNullElse(TardisLogic.getVariant(entity.getTardisId()), TardisChameleonVariant.TT_CAPSULE);
        TardisDoorState doorState = Objects.requireNonNullElse(TardisLogic.getDoorState(entity.getTardisId()), new TardisDoorState());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(textureCache.get(variant)));
        this.render(matrices, vertexConsumer, modelCache.get(variant), doorState.doorSwing, RotationPropertyHelper.toDegrees(rotation), light, overlay);
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
