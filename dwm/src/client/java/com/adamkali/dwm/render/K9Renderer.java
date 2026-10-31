package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.K9Entity;
import com.adamkali.dwm.model.entity.K9Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class K9Renderer extends MobRenderer<K9Entity, LivingEntityRenderState, K9Model> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/k9.png");

    public K9Renderer(EntityRendererProvider.Context context) {
        super(context, new K9Model(context.bakeLayer(K9Model.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
