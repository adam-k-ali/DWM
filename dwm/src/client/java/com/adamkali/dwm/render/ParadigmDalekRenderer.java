package com.adamkali.dwm.render;

import com.adamkali.dwm.model.entity.ParadigmDalekModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ParadigmDalekRenderer extends DalekRenderer {
    private static final float GROUND_SHADOW_RADIUS = 0.75F;

    public ParadigmDalekRenderer(EntityRendererProvider.Context context) {
        super(context, new ParadigmDalekModel(context.bakeLayer(ParadigmDalekModel.LAYER_LOCATION)), GROUND_SHADOW_RADIUS);
    }
}
