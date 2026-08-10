package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.model.tileentity.*;
import com.adamkali.dwm.render.boti.TardisBotiRenderer;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public class TardisBlockEntityRenderer implements BlockEntityRenderer<TardisBlockEntity> {
    private final HashMap<TardisChameleonVariant, TardisModel> modelCache = new HashMap<>();
    private final HashMap<TardisChameleonVariant, Identifier> textureCache = new HashMap<>();

    private void saveChameleonVariant(TardisChameleonVariant variant, TardisModel model, Identifier texture) {
        modelCache.put(variant, model);
        textureCache.put(variant, texture);
    }

    public TardisBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        saveChameleonVariant(TardisChameleonVariant.TT_CAPSULE, new TTCapsuleModel(context.bakeLayer(TTCapsuleModel.LAYER_LOCATION)), TTCapsuleModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.FIRST_DOCTOR_BOX, new FirstDoctorTardisModel(context.bakeLayer(FirstDoctorTardisModel.LAYER_LOCATION)), FirstDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.SECOND_DOCTOR_BOX, new SecondDoctorTardisModel(context.bakeLayer(SecondDoctorTardisModel.LAYER_LOCATION)), SecondDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.THIRD_DOCTOR_BOX, new ThirdDoctorTardisModel(context.bakeLayer(ThirdDoctorTardisModel.LAYER_LOCATION)), ThirdDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.FOURTH_DOCTOR_BOX, new FourthDoctorTardisModel(context.bakeLayer(FourthDoctorTardisModel.LAYER_LOCATION)), FourthDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.FIFTH_DOCTOR_BOX, new FifthDoctorTardisModel(context.bakeLayer(FifthDoctorTardisModel.LAYER_LOCATION)), FifthDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.SIXTH_DOCTOR_BOX, new SixthDoctorTardisModel(context.bakeLayer(SixthDoctorTardisModel.LAYER_LOCATION)), SixthDoctorTardisModel.TEXTURE_LOCATION);
        saveChameleonVariant(TardisChameleonVariant.SEVENTH_DOCTOR_BOX, new SeventhDoctorTardisModel(context.bakeLayer(SeventhDoctorTardisModel.LAYER_LOCATION)), SeventhDoctorTardisModel.TEXTURE_LOCATION);
    }

    @Override
    public void render(TardisBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        BlockState state = entity.getBlockState();
        int rotation = state.getValueOrElse(TardisBlock.FACING_ROTATION, 0);

        TardisChameleonVariant variant = Objects.requireNonNullElse(TardisLogic.getVariant(entity.getTardisId()), TardisChameleonVariant.TT_CAPSULE);
        TardisDoorState doorState = Objects.requireNonNullElse(TardisLogic.getDoorState(entity.getTardisId()), new TardisDoorState());
        float degrees = RotationSegment.convertToDegrees(rotation);

        TardisModel model = modelCache.get(variant);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(textureCache.get(variant)));

        if (TardisBotiRenderer.shouldRender(doorState)) {
            // Shell → BOTI → doors so swung doors draw over the interior preview.
            this.renderShell(matrices, vertexConsumer, model, doorState.doorSwing, degrees, light, overlay);

            matrices.pushPose();
            applyExteriorTransforms(matrices, degrees);
            TardisBotiRenderer.render(matrices, vertexConsumers, tickDelta, entity.getTardisId(), variant);
            matrices.popPose();

            vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(textureCache.get(variant)));
            this.renderDoors(matrices, vertexConsumer, model, doorState.doorSwing, degrees, light, overlay);
        } else {
            this.renderExterior(matrices, vertexConsumer, model, doorState.doorSwing, degrees, light, overlay);
        }
    }

    @Override
    public int getViewDistance() {
        // Interior preview extends several blocks behind the door aperture.
        return 128;
    }

    private void renderExterior(PoseStack matrices, VertexConsumer vertices, TardisModel model, float doorProgress, float rotation, int light, int overlay) {
        prepareRenderState(model, doorProgress);
        matrices.pushPose();
        applyExteriorTransforms(matrices, rotation);
        model.renderToBuffer(matrices, vertices, light, overlay);
        matrices.popPose();
    }

    private void renderShell(PoseStack matrices, VertexConsumer vertices, TardisModel model, float doorProgress, float rotation, int light, int overlay) {
        prepareRenderState(model, doorProgress);
        matrices.pushPose();
        applyExteriorTransforms(matrices, rotation);
        model.renderShell(matrices, vertices, light, overlay);
        matrices.popPose();
    }

    private void renderDoors(PoseStack matrices, VertexConsumer vertices, TardisModel model, float doorProgress, float rotation, int light, int overlay) {
        prepareRenderState(model, doorProgress);
        matrices.pushPose();
        applyExteriorTransforms(matrices, rotation);
        model.renderDoors(matrices, vertices, light, overlay);
        matrices.popPose();
    }

    private static TardisRenderState prepareRenderState(TardisModel model, float doorProgress) {
        TardisRenderState state = new TardisRenderState();
        state.setDoorSwingProgress(doorProgress);
        model.setupAnim(state);
        return state;
    }

    private static void applyExteriorTransforms(PoseStack matrices, float rotationDegrees) {
        matrices.scale(2.0f, 2.0f, 2.0f);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0f));
        matrices.translate(0.25D, -1.5D, -0.25D);
        matrices.mulPose(Axis.YP.rotationDegrees(rotationDegrees - 180.0f));
    }
}
