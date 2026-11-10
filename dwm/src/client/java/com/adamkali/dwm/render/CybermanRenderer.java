package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.CybermanEntity;
import com.adamkali.dwm.model.entity.CybermanModel;
import com.adamkali.dwm.model.json.EntityModelTextures;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/** Renders one Cyberman type with its JSON layer and the texture that layer declares. */
public class CybermanRenderer<T extends CybermanEntity>
        extends MobRenderer<T, LivingEntityRenderState, CybermanModel> {
    private final ModelLayerLocation layer;

    public CybermanRenderer(
            EntityRendererProvider.Context context,
            ModelLayerLocation layer,
            Function<ModelPart, ? extends CybermanModel> modelFactory
    ) {
        super(context, modelFactory.apply(context.bakeLayer(layer)), 0.5F);
        this.layer = layer;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return EntityModelTextures.get(layer);
    }
}
