package com.adamkali.dwm.render;

import com.adamkali.dwm.model.json.EntityModelTextures;
import com.adamkali.dwm.entity.DalekLaserEntity;
import com.adamkali.dwm.model.entity.DalekLaserModel;
import com.adamkali.dwm.render.state.DalekLaserRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DalekLaserRenderer extends EntityRenderer<DalekLaserEntity, DalekLaserRenderState> {
    private final DalekLaserModel model;
    private final Identifier texture;

    public DalekLaserRenderer(EntityRendererProvider.Context context) {
        this.texture = EntityModelTextures.get(DalekLaserModel.LAYER_LOCATION);
        super(context);
        this.model = new DalekLaserModel(context.bakeLayer(DalekLaserModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void submit(
            DalekLaserRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.08F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        poseStack.scale(0.55F, 0.55F, 0.55F);
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                texture,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public DalekLaserRenderState createRenderState() {
        return new DalekLaserRenderState();
    }

    @Override
    public void extractRenderState(DalekLaserEntity entity, DalekLaserRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
    }
}
