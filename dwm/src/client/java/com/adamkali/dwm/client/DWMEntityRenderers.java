package com.adamkali.dwm.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.model.armor.EvaSuitModel;
import com.adamkali.dwm.model.entity.BroakirModel;
import com.adamkali.dwm.model.entity.DalekLaserModel;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.model.entity.TimeLordModel;
import com.adamkali.dwm.model.json.JsonEntityModelLayers;
import com.adamkali.dwm.render.BroakirRenderer;
import com.adamkali.dwm.render.DalekLaserRenderer;
import com.adamkali.dwm.render.DalekRenderer;
import com.adamkali.dwm.render.FlutterwingRenderer;
import com.adamkali.dwm.render.MewingDogRenderer;
import com.adamkali.dwm.render.EvaSuitArmorRenderer;
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
        ModelLayerRegistry.registerArmorModelLayers(
                EvaSuitModel.LAYER_SET,
                EvaSuitModel::createArmorLayerSet
        );
        EvaSuitArmorRenderer.register();

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
        JsonEntityModelLayers.register(BroakirModel.LAYER_LOCATION);
        EntityRendererRegistry.register(DWMEntityTypes.BROAKIR, BroakirRenderer::new);
        JsonEntityModelLayers.register(FlutterwingModel.LAYER_LOCATION);
        EntityRendererRegistry.register(DWMEntityTypes.FLUTTERWING, FlutterwingRenderer::new);
        EntityRendererRegistry.register(DWMEntityTypes.MEWING_DOG, MewingDogRenderer::new);
        JsonEntityModelLayers.register(TimeLordModel.LAYER_LOCATION);
        EntityRendererRegistry.register(DWMEntityTypes.TIME_LORD, TimeLordRenderer::new);
        JsonEntityModelLayers.register(DalekModel.LAYER_LOCATION);
        EntityRendererRegistry.register(DWMEntityTypes.DALEK, DalekRenderer::new);
        JsonEntityModelLayers.register(DalekLaserModel.LAYER_LOCATION);
        EntityRendererRegistry.register(DWMEntityTypes.DALEK_LASER, DalekLaserRenderer::new);
    }
}
