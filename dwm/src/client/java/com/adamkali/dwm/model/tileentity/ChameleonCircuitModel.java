package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/**
 * Basic chameleon-circuit dial on Panel6. Geometry lives in {@link ConsoleSelectorModel}.
 * Texture uses archive spelling {@code chameleion_circuit_on}.
 */
public class ChameleonCircuitModel extends ConsoleSelectorModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "chameleon_circuit"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/chameleion_circuit_on.png");

    public ChameleonCircuitModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        return ConsoleSelectorModel.getTexturedModelData();
    }
}
