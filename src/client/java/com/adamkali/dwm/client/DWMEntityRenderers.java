package com.adamkali.dwm.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class DWMEntityRenderers {
    private DWMEntityRenderers() {
    }

    public static void initialize() {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            ModelLayerLocation layer = new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "boat/" + family.definition().id()),
                    "main"
            );
            EntityModelLayerRegistry.registerModelLayer(layer, BoatModel::createBoatModel);
            EntityRendererRegistry.register(
                    family.boatEntity(),
                    context -> new BoatRenderer(context, layer)
            );
        }
    }
}
