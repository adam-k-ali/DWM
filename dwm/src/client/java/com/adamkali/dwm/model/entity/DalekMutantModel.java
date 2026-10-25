package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Kaled mutant: fleshy dome with a brain, one eye and six tentacles. Mesh and animation live in
 * {@code models/entity/dalek_mutant.json}.
 */
public class DalekMutantModel extends JsonEntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek_mutant"), "main");

    private static final AnimationVariables<LivingEntityRenderState> VARIABLES = AnimationVariables.<LivingEntityRenderState>living().build();

    public DalekMutantModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), VARIABLES);
    }
}
