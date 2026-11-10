package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/** Weapons-grade Cyberman: sleek armour with slim handlebars. Mesh in {@code models/entity/weapons_grade_cyberman.json}. */
public class WeaponsGradeCybermanModel extends CybermanModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "weapons_grade_cyberman"), "main");

    public WeaponsGradeCybermanModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
