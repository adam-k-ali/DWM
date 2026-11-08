package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Panel6 bottom-row stabilisers control. Lever pitch reflects on/off state through the JSON
 * {@code animation} ({@code stabilisers}, 0 or 1): the rest pose is "on", off flattens the lever.
 */
public class StabilisersModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "stabilisers"), "main");

    public StabilisersModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
