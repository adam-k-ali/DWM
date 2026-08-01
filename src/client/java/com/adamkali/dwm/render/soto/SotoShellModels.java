package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.model.tileentity.FifthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FourthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SecondDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SeventhDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SixthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.TTCapsuleModel;
import com.adamkali.dwm.model.tileentity.TardisModel;
import com.adamkali.dwm.model.tileentity.ThirdDoctorTardisModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Chameleon shell models for SOTO exterior preview (loaded once from interior-door BER context).
 */
public final class SotoShellModels {
    private final Map<TardisChameleonVariant, TardisModel> models = new EnumMap<>(TardisChameleonVariant.class);
    private final Map<TardisChameleonVariant, Identifier> textures = new EnumMap<>(TardisChameleonVariant.class);

    public SotoShellModels(BlockEntityRendererFactory.Context context) {
        put(TardisChameleonVariant.TT_CAPSULE, new TTCapsuleModel(context.getLayerModelPart(TTCapsuleModel.LAYER_LOCATION)), TTCapsuleModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.FIRST_DOCTOR_BOX, new FirstDoctorTardisModel(context.getLayerModelPart(FirstDoctorTardisModel.LAYER_LOCATION)), FirstDoctorTardisModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.SECOND_DOCTOR_BOX, new SecondDoctorTardisModel(context.getLayerModelPart(SecondDoctorTardisModel.LAYER_LOCATION)), SecondDoctorTardisModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.THIRD_DOCTOR_BOX, new ThirdDoctorTardisModel(context.getLayerModelPart(ThirdDoctorTardisModel.LAYER_LOCATION)), ThirdDoctorTardisModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.FOURTH_DOCTOR_BOX, new FourthDoctorTardisModel(context.getLayerModelPart(FourthDoctorTardisModel.LAYER_LOCATION)), FourthDoctorTardisModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.FIFTH_DOCTOR_BOX, new FifthDoctorTardisModel(context.getLayerModelPart(FifthDoctorTardisModel.LAYER_LOCATION)), FifthDoctorTardisModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.SIXTH_DOCTOR_BOX, new SixthDoctorTardisModel(context.getLayerModelPart(SixthDoctorTardisModel.LAYER_LOCATION)), SixthDoctorTardisModel.TEXTURE_LOCATION);
        put(TardisChameleonVariant.SEVENTH_DOCTOR_BOX, new SeventhDoctorTardisModel(context.getLayerModelPart(SeventhDoctorTardisModel.LAYER_LOCATION)), SeventhDoctorTardisModel.TEXTURE_LOCATION);
    }

    private void put(TardisChameleonVariant variant, TardisModel model, Identifier texture) {
        models.put(variant, model);
        textures.put(variant, texture);
    }

    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay,
            TardisChameleonVariant variant,
            float doorSwing,
            int exteriorRotation
    ) {
        TardisChameleonVariant resolved = Objects.requireNonNullElse(variant, TardisChameleonVariant.TT_CAPSULE);
        TardisModel model = models.get(resolved);
        Identifier texture = textures.get(resolved);
        if (model == null || texture == null) {
            return;
        }
        TardisRenderState state = new TardisRenderState();
        state.setDoorSwingProgress(doorSwing);
        model.setAngles(state);

        float degrees = RotationPropertyHelper.toDegrees(exteriorRotation);
        VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
        matrices.push();
        applyExteriorTransforms(matrices, degrees);
        model.render(matrices, vertices, light, overlay);
        matrices.pop();
    }

    /** Same transforms as {@code TardisBlockEntityRenderer.applyExteriorTransforms}. */
    static void applyExteriorTransforms(MatrixStack matrices, float rotationDegrees) {
        matrices.scale(2.0f, 2.0f, 2.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        matrices.translate(0.25D, -1.5D, -0.25D);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees - 180.0f));
    }
}
