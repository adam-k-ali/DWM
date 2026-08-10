package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.model.tileentity.FifthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FourthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SecondDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SeventhDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SixthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.TTCapsuleModel;
import com.adamkali.dwm.model.tileentity.TardisModel;
import com.adamkali.dwm.model.tileentity.ThirdDoctorTardisModel;
import com.adamkali.dwm.render.state.TardisBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.TardisBotiGate;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;

public class TardisBlockEntityRenderer implements BlockEntityRenderer<TardisBlockEntity, TardisBlockEntityRenderState> {
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
    public TardisBlockEntityRenderState createRenderState() {
        return new TardisBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            TardisBlockEntity entity,
            TardisBlockEntityRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = entity.getBlockState();
        int rotation = blockState.getValueOrElse(TardisBlock.FACING_ROTATION, 0);
        TardisDoorState doorState = Objects.requireNonNullElse(TardisLogic.getDoorState(entity.getTardisId()), new TardisDoorState());

        state.variant = Objects.requireNonNullElse(TardisLogic.getVariant(entity.getTardisId()), TardisChameleonVariant.TT_CAPSULE);
        state.doorSwing = doorState.doorSwing;
        state.rotationDegrees = RotationSegment.convertToDegrees(rotation);
        state.partialTicks = partialTicks;
        state.tardisId = entity.getTardisId();
        state.shouldRenderBoti = shouldRenderBoti(doorState);
    }

    @Override
    public void submit(
            TardisBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        TardisModel model = modelCache.get(state.variant);
        Identifier texture = textureCache.get(state.variant);
        if (model == null || texture == null) {
            return;
        }

        TardisRenderState animState = new TardisRenderState();
        animState.setDoorSwingProgress(state.doorSwing);

        if (state.shouldRenderBoti) {
            // TODO(boti): restore shell → BOTI → doors once TardisBotiRenderer accepts SubmitNodeCollector.
            submitBotiPlaceholder(state, poseStack, submitNodeCollector);
        }
        submitExterior(model, animState, poseStack, submitNodeCollector, texture, state);
    }

    @Override
    public int getViewDistance() {
        // Interior preview extends several blocks behind the door aperture.
        return 128;
    }

    private void submitExterior(
            TardisModel model,
            TardisRenderState animState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Identifier texture,
            TardisBlockEntityRenderState state
    ) {
        poseStack.pushPose();
        applyExteriorTransforms(poseStack, state.rotationDegrees);
        submitNodeCollector.submitModel(
                model,
                animState,
                poseStack,
                RenderTypes.entityCutout(texture),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();
    }

    /**
     * Compile-safe BOTI stand-in. Sibling agents own {@code render/boti/**}; re-hook when their
     * submit-era API is ready.
     */
    // TODO(boti): call TardisBotiRenderer with SubmitNodeCollector once available.
    private static void submitBotiPlaceholder(
            TardisBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        // no-op: MultiBufferSource / immediate flush path removed in 26.2 submit pipeline
    }

    /**
     * Stencil support lives in the BOTI package (sibling-owned); gate on config + door state only here.
     */
    private static boolean shouldRenderBoti(TardisDoorState doorState) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_BOTI) && TardisBotiGate.shouldShow(doorState);
    }

    private static void applyExteriorTransforms(PoseStack matrices, float rotationDegrees) {
        matrices.scale(2.0f, 2.0f, 2.0f);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0f));
        matrices.translate(0.25D, -1.5D, -0.25D);
        matrices.mulPose(Axis.YP.rotationDegrees(rotationDegrees - 180.0f));
    }
}
