package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Player-locator dial on Panel3. Geometry lives in {@link ConsoleSelectorModel}.
 */
public class PlayerLocatorModel extends ConsoleSelectorModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "player_locator"), "main");

    public PlayerLocatorModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
