package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/** Cybermaster: Gallifreyan high collar and wrist-laser gauntlet. Mesh in {@code models/entity/cybermaster.json}. */
public class CybermasterModel extends CybermanModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cybermaster"), "main");

    public CybermasterModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
