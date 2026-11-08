// Made with Blockbench (converted from fast_return.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

public class FastReturnModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "fast_return"), "main");

    public FastReturnModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
