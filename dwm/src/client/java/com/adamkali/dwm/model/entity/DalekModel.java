package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import com.adamkali.dwm.entity.DalekFlightFx;
import com.adamkali.dwm.render.state.DalekRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Mesh and animation live in {@code models/entity/dalek.json}. The skin comes from the entity
 * variant; the JSON {@code texture} is the default variant.
 */
public class DalekModel extends JsonEntityModel<DalekRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek"), "main");

    private static final AnimationVariables<DalekRenderState> VARIABLES = AnimationVariables.<DalekRenderState>living()
            .add("lean_pitch", state -> state.leanPitch)
            .add("lean_roll", state -> state.leanRoll)
            .add("flight_bob", state -> bobOffset(state.ageInTicks, state.flying))
            .build();

    public DalekModel(ModelPart root) {
        super(root, LAYER_LOCATION, VARIABLES);
    }

    public static float bobOffset(float ageInTicks, boolean flying) {
        return DalekFlightFx.bobOffset(ageInTicks, flying);
    }
}
