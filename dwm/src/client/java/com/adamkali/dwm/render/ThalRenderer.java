package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.model.entity.ThalModel;
import com.adamkali.dwm.render.state.ThalRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class ThalRenderer extends MobRenderer<ThalEntity, ThalRenderState, ThalModel> {
    public ThalRenderer(EntityRendererProvider.Context context) {
        super(context, new ThalModel(context.bakeLayer(ThalModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ThalRenderState createRenderState() {
        return new ThalRenderState();
    }

    @Override
    public void extractRenderState(ThalEntity entity, ThalRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.variant = entity.getVariant();
    }

    @Override
    public Identifier getTextureLocation(ThalRenderState state) {
        return state.variant.textureLocation();
    }
}
