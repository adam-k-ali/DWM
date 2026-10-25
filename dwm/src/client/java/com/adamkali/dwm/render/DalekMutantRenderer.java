package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.DalekMutantEntity;
import com.adamkali.dwm.model.entity.DalekMutantModel;
import com.adamkali.dwm.model.json.EntityModelTextures;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class DalekMutantRenderer extends MobRenderer<DalekMutantEntity, LivingEntityRenderState, DalekMutantModel> {
    public DalekMutantRenderer(EntityRendererProvider.Context context) {
        super(context, new DalekMutantModel(context.bakeLayer(DalekMutantModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return EntityModelTextures.get(DalekMutantModel.LAYER_LOCATION);
    }
}
