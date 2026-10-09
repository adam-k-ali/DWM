package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Mesh and animation live in {@code models/entity/broakir.json}.
 */
public class BroakirModel extends JsonEntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "broakir"), "main");

    private static final AnimationVariables<LivingEntityRenderState> VARIABLES = AnimationVariables.<LivingEntityRenderState>living().build();

    public BroakirModel(ModelPart root) {
        super(root, LAYER_LOCATION, VARIABLES);
    }
}
