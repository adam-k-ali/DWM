package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Shared dial mesh for console selectors (biome / waypoint / player / planet / chameleon).
 */
public class ConsoleSelectorModel extends JsonEntityModel<TardisRenderState> {
    public static final String PART_NAME = "console_selector";

    protected ConsoleSelectorModel(ModelPart root, ModelLayerLocation layer) {
        super(root, EntityModelAnimations.get(layer), TardisControlVariables.VARIABLES);
    }
}
