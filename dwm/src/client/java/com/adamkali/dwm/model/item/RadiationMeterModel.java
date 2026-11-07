package com.adamkali.dwm.model.item;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.tileentity.TardisControlVariables;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Chunky handheld radiation meter with a raised screen and industrial controls.
 */
public class RadiationMeterModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "radiation_meter"),
                    "main");

    public RadiationMeterModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
