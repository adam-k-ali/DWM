package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DWMRenderLayerManager.initialize();
        ClientAnalyticsManager.initialize();
        DWMBlockEntityRendererFactories.initialize();
    }


}