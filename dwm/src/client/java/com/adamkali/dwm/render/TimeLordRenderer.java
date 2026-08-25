package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.TimeLordEntity;
import com.adamkali.dwm.model.entity.TimeLordModel;
import com.adamkali.dwm.render.state.TimeLordRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class TimeLordRenderer extends MobRenderer<TimeLordEntity, TimeLordRenderState, TimeLordModel> {
    public TimeLordRenderer(EntityRendererProvider.Context context) {
        super(context, new TimeLordModel(context.bakeLayer(TimeLordModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public TimeLordRenderState createRenderState() {
        return new TimeLordRenderState();
    }

    @Override
    public void extractRenderState(TimeLordEntity entity, TimeLordRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.variant = entity.getVariant();
    }

    @Override
    public Identifier getTextureLocation(TimeLordRenderState state) {
        return state.variant.textureLocation();
    }
}
