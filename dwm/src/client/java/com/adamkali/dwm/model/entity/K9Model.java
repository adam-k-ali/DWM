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
 * K-9 robot dog: box chassis (no legs; wheels are hidden), nodding head, swivelling ears and a tail
 * antenna. Mesh and animation live in {@code models/entity/k9.json}.
 */
public class K9Model extends JsonEntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "k9"), "main");

    private static final AnimationVariables<LivingEntityRenderState> VARIABLES =
            AnimationVariables.<LivingEntityRenderState>living().build();

    public K9Model(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), VARIABLES);
    }
}
