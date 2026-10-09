package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Mesh and animation live in {@code models/entity/time_lord.json}. The skin comes from the
 * entity variant; the JSON {@code texture} is the default variant.
 */
public class TimeLordModel extends JsonEntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "time_lord"), "main");

    private static final AnimationVariables<LivingEntityRenderState> VARIABLES = AnimationVariables.<LivingEntityRenderState>living().build();

    public TimeLordModel(ModelPart root) {
        super(root, LAYER_LOCATION, VARIABLES);
    }
}
