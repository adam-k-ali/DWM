package com.adamkali.dwm;

import com.adamkali.dwm.network.ClientPayloadTypeRegistry;
import com.adamkali.dwm.client.DWMEntityRenderers;
import com.adamkali.dwm.render.ConsoleControlHud;
import com.adamkali.dwm.render.ConsoleHitboxDebugRenderer;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.portal.SotoPortalSupport;
import com.adamkali.dwm.sound.TardisHumController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DWMRenderLayerManager.initialize();
        DWMEntityRenderers.initialize();
        ClientAnalyticsManager.initialize();
        DWMBlockEntityRendererFactories.initialize();
        ClientPayloadTypeRegistry.initialize();
        TardisHumController.initialize();
        ConsoleControlHud.initialize();
        ConsoleHitboxDebugRenderer.initialize();
        SotoPortalSupport.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> SotoGhostExterior.clientTick());
    }
}
