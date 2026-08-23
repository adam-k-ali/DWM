package com.adamkali.dwm.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.model.entity.BroakirModel;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.platform.DwmClientPlatform;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.render.BroakirRenderer;
import com.adamkali.dwm.render.FlutterwingRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.Identifier;

public final class DWMEntityRenderers {
    private DWMEntityRenderers() {
    }

    public static void initialize() {
        DwmClientPlatform platform = DwmClientServices.get();
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            ModelLayerLocation layer = new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "boat/" + family.definition().id()),
                    "main"
            );
            platform.registerModelLayer(layer, BoatModel::createBoatModel);
            platform.registerEntityRenderer(
                    family.boatEntity(),
                    context -> new BoatRenderer(context, layer)
            );
        }
        platform.registerEntityRenderer(DWMEntityTypes.TARDIS_SEAT, NoopRenderer::new);
        platform.registerEntityRenderer(DWMEntityTypes.CONSOLE_CONTROL, NoopRenderer::new);
        platform.registerModelLayer(BroakirModel.LAYER_LOCATION, BroakirModel::createBodyLayer);
        platform.registerEntityRenderer(DWMEntityTypes.BROAKIR, BroakirRenderer::new);
        platform.registerModelLayer(FlutterwingModel.LAYER_LOCATION, FlutterwingModel::createBodyLayer);
        platform.registerEntityRenderer(DWMEntityTypes.FLUTTERWING, FlutterwingRenderer::new);
    }
}
