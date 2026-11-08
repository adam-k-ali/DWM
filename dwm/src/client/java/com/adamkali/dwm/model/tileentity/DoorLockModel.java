// Made with Blockbench (converted from door_lock.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Panel4 bottom-row door lock.
 */
public class DoorLockModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "door_lock"), "main");

    public DoorLockModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
