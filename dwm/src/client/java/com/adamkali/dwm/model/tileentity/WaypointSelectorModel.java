package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/**
 * Waypoint-selector dial on Panel3. Geometry lives in {@link ConsoleSelectorModel}.
 */
public class WaypointSelectorModel extends ConsoleSelectorModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint_selector"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/waypoint_selector.png");

    public WaypointSelectorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        return ConsoleSelectorModel.getTexturedModelData();
    }
}
