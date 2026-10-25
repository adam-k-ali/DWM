package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Taller Paradigm casing. Mesh and animation live in {@code models/entity/paradigm_dalek.json};
 * part names and animation variables match {@link DalekModel}. The skin comes from the caste variant.
 */
public class ParadigmDalekModel extends DalekModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "paradigm_dalek"), "main");

    public ParadigmDalekModel(ModelPart root) {
        super(root, LAYER_LOCATION);
    }
}
