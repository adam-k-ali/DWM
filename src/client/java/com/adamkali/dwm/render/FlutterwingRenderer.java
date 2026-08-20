package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.FlutterwingEntity;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.render.state.FlutterwingRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class FlutterwingRenderer extends MobRenderer<FlutterwingEntity, FlutterwingRenderState, FlutterwingModel> {
    public FlutterwingRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FlutterwingModel(context.bakeLayer(FlutterwingModel.LAYER_LOCATION)),
                0.5F * FlutterwingEntity.SCALE
        );
    }

    @Override
    public FlutterwingRenderState createRenderState() {
        return new FlutterwingRenderState();
    }

    @Override
    public void extractRenderState(FlutterwingEntity entity, FlutterwingRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.variant = entity.getVariant();
    }

    @Override
    protected void scale(FlutterwingRenderState state, PoseStack poseStack) {
        poseStack.scale(FlutterwingEntity.SCALE, FlutterwingEntity.SCALE, FlutterwingEntity.SCALE);
    }

    @Override
    public Identifier getTextureLocation(FlutterwingRenderState state) {
        return state.variant.textureLocation();
    }
}
