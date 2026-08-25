package com.adamkali.dwm.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.model.entity.BroakirModel;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.model.entity.TimeLordModel;
import com.adamkali.dwm.render.BroakirRenderer;
import com.adamkali.dwm.render.FlutterwingRenderer;
import com.adamkali.dwm.render.MewingDogRenderer;
import com.adamkali.dwm.render.TimeLordRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
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
            ModelLayerRegistry.registerModelLayer(layer, BoatModel::createBoatModel);
            EntityRendererRegistry.register(
                    family.boatEntity(),
                    context -> new BoatRenderer(context, layer)
            );
        }
        EntityRendererRegistry.register(DWMEntityTypes.TARDIS_SEAT, NoopRenderer::new);
        EntityRendererRegistry.register(DWMEntityTypes.CONSOLE_CONTROL, NoopRenderer::new);
        ModelLayerRegistry.registerModelLayer(BroakirModel.LAYER_LOCATION, BroakirModel::createBodyLayer);
        EntityRendererRegistry.register(DWMEntityTypes.BROAKIR, BroakirRenderer::new);
        ModelLayerRegistry.registerModelLayer(FlutterwingModel.LAYER_LOCATION, FlutterwingModel::createBodyLayer);
        EntityRendererRegistry.register(DWMEntityTypes.FLUTTERWING, FlutterwingRenderer::new);
        EntityRendererRegistry.register(DWMEntityTypes.MEWING_DOG, MewingDogRenderer::new);
        ModelLayerRegistry.registerModelLayer(TimeLordModel.LAYER_LOCATION, TimeLordModel::createBodyLayer);
        EntityRendererRegistry.register(DWMEntityTypes.TIME_LORD, TimeLordRenderer::new);
    }
}
