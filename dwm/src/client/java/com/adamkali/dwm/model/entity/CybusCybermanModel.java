package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/** Cybus Cyberman: bulkier steel armour and handlebars. Mesh in {@code models/entity/cybus_cyberman.json}. */
public class CybusCybermanModel extends CybermanModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cybus_cyberman"), "main");

    public CybusCybermanModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
