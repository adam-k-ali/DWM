package com.adamkali.dwm;

import com.adamkali.dwm.network.ClientPayloadTypeRegistry;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DWMRenderLayerManager.initialize();
        ClientAnalyticsManager.initialize();
        DWMBlockEntityRendererFactories.initialize();
        ClientPayloadTypeRegistry.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> SotoGhostExterior.clientTick());
    }
}
