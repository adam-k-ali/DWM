package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Humanoid Cyberman mesh. Every Cyberman type has its own JSON layer
 * ({@code models/entity/<id>.json}) that inherits the base body and march animation from
 * {@code models/entity/cyberman.json} and overrides the parts that make the type distinct.
 */
public abstract class CybermanModel extends JsonEntityModel<LivingEntityRenderState> {
    private static final AnimationVariables<LivingEntityRenderState> VARIABLES =
            AnimationVariables.<LivingEntityRenderState>living().build();

    protected CybermanModel(ModelPart root, ModelLayerLocation layer) {
        super(root, EntityModelAnimations.get(layer), VARIABLES);
    }
}
