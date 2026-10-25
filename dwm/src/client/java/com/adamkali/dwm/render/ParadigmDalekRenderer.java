package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.model.entity.ParadigmDalekModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.Map;

public class ParadigmDalekRenderer extends DalekRenderer {
    private static final float GROUND_SHADOW_RADIUS = 0.75F;

    public ParadigmDalekRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                Map.of(DalekChassis.PARADIGM, new ParadigmDalekModel(context.bakeLayer(ParadigmDalekModel.LAYER_LOCATION))),
                DalekChassis.PARADIGM,
                GROUND_SHADOW_RADIUS
        );
        this.addLayer(new ParadigmDalekGlowLayer(this));
    }
}
