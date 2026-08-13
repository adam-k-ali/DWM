package com.adamkali.dwm;

import com.adamkali.dwm.network.ClientPayloadTypeRegistry;
import com.adamkali.dwm.client.DWMEntityRenderers;
import com.adamkali.dwm.render.ConsoleControlHud;
import com.adamkali.dwm.render.ConsoleHitboxDebugRenderer;
import com.adamkali.dwm.render.portal.PortalPerfDebugHud;
import com.adamkali.dwm.render.portal.PortalPerfDebugLog;
import com.adamkali.dwm.render.portal.PortalSupport;
import com.adamkali.dwm.render.portal.PortalSceneStore;
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
        PortalPerfDebugHud.initialize();
        PortalPerfDebugLog.resetForSession();
        PortalSupport.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> PortalSceneStore.clientTick());
    }
}
