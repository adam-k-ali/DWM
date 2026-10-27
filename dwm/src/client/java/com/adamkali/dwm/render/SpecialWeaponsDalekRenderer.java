package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.model.entity.SpecialWeaponsDalekModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.Map;

public class SpecialWeaponsDalekRenderer extends DalekRenderer {
    private static final float GROUND_SHADOW_RADIUS = 0.75F;

    public SpecialWeaponsDalekRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                Map.of(DalekChassis.SPECIAL_WEAPONS, new SpecialWeaponsDalekModel(context.bakeLayer(SpecialWeaponsDalekModel.LAYER_LOCATION))),
                DalekChassis.SPECIAL_WEAPONS,
                GROUND_SHADOW_RADIUS
        );
    }
}
