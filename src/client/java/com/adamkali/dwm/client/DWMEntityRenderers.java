package com.adamkali.dwm.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class DWMEntityRenderers {
    private DWMEntityRenderers() {
    }

    public static void initialize() {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            EntityModelLayer layer = new EntityModelLayer(
                    Identifier.of(DWMReference.MOD_ID, "boat/" + family.definition().id()),
                    "main"
            );
            EntityModelLayerRegistry.registerModelLayer(layer, BoatEntityModel::getTexturedModelData);
            EntityRendererRegistry.register(
                    family.boatEntity(),
                    context -> new BoatEntityRenderer(context, layer)
            );
        }
    }
}
