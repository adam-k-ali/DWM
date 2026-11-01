package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import com.adamkali.dwm.render.state.ThalRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Mesh and animation live in {@code models/entity/thal.json}. The skin comes from the
 * entity variant; the JSON {@code texture} is the default variant.
 */
public class ThalModel extends JsonEntityModel<ThalRenderState> implements ArmedModel<ThalRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "thal"), "main");

    private static final AnimationVariables<ThalRenderState> VARIABLES = AnimationVariables.<ThalRenderState>living()
            .add("armed", state -> state.armed ? 1.0 : 0.0)
            .build();

    public ThalModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), VARIABLES);
    }

    @Override
    public void translateToHand(ThalRenderState state, HumanoidArm arm, PoseStack poseStack) {
        this.root().getChild(arm == HumanoidArm.RIGHT ? "right_arm" : "left_arm").translateAndRotate(poseStack);
    }
}
