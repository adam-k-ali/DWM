// Made with Blockbench (converted from tardis_cloak.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Panel4 cloak / perception-filter lever.
 */
public class CloakLeverModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cloak_lever"), "main");

    public CloakLeverModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
