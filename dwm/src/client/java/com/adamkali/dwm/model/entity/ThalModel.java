package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Mesh and animation live in {@code models/entity/thal.json}. The skin comes from the
 * entity variant; the JSON {@code texture} is the default variant.
 */
public class ThalModel extends JsonEntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "thal"), "main");

    private static final AnimationVariables<LivingEntityRenderState> VARIABLES = AnimationVariables.<LivingEntityRenderState>living().build();

    public ThalModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), VARIABLES);
    }
}
