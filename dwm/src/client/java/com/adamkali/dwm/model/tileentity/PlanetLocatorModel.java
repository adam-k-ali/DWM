package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/**
 * Planet-locator dial on Panel3. Geometry lives in {@link ConsoleSelectorModel}.
 */
public class PlanetLocatorModel extends ConsoleSelectorModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "planet_locator"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/planet_locator.png");

    public PlanetLocatorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        return ConsoleSelectorModel.getTexturedModelData();
    }
}
