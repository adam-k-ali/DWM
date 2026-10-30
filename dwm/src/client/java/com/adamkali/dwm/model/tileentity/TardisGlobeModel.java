package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import com.adamkali.dwm.render.state.TardisGlobeRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Armillary-sphere globe. Motion comes from the JSON {@code animation}: {@code age} (ambient clock,
 * ticks), {@code spin} (interaction turn, degrees) and {@code energy} (0-1 interaction envelope).
 */
public class TardisGlobeModel extends JsonEntityModel<TardisGlobeRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_globe"), "main");
    /** Full-bright overlay: only the core, moon, finial gem and plinth lamps are opaque. */
    public static final Identifier GLOW_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_globe_glow.png");

    public static final AnimationVariables<TardisGlobeRenderState> VARIABLES = AnimationVariables.<TardisGlobeRenderState>builder()
            .add("age", state -> state.ageInTicks)
            .add("spin", state -> state.spinDegrees)
            .add("energy", state -> state.spinEnergy)
            .build();

    public TardisGlobeModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), VARIABLES);
    }
}
