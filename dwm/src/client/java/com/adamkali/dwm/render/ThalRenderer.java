package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.model.entity.ThalModel;
import com.adamkali.dwm.render.state.ThalRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;

public class ThalRenderer extends MobRenderer<ThalEntity, ThalRenderState, ThalModel> {
    private final ItemModelResolver itemModelResolver;

    public ThalRenderer(EntityRendererProvider.Context context) {
        super(context, new ThalModel(context.bakeLayer(ThalModel.LAYER_LOCATION)), 0.5F);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public ThalRenderState createRenderState() {
        return new ThalRenderState();
    }

    @Override
    public void extractRenderState(ThalEntity entity, ThalRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, tickDelta);
        state.variant = entity.getVariant();
        state.armed = entity.isArmed();
    }

    @Override
    public Identifier getTextureLocation(ThalRenderState state) {
        return state.variant.textureLocation();
    }
}
