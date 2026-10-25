package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * 2005 Time War casing on the classic family. Mesh and animation live in
 * {@code models/entity/time_war_dalek.json}; part names and animation variables match
 * {@link DalekModel}, plus shoulder {@code slats}. The skin comes from the bronze variant.
 */
public class TimeWarDalekModel extends DalekModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "time_war_dalek"), "main");

    public TimeWarDalekModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
