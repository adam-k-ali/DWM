package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Heavy Special Weapons casing (cannon and weapon pack, no plunger). Mesh and animation live in
 * {@code models/entity/special_weapons_dalek.json}; part names and animation variables match
 * {@link DalekModel}.
 */
public class SpecialWeaponsDalekModel extends DalekModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "special_weapons_dalek"), "main");

    public SpecialWeaponsDalekModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
