package com.adamkali.dwm;

import com.adamkali.dwm.network.ClientPayloadTypeRegistry;
import com.adamkali.dwm.client.DWMEntityRenderers;
import com.adamkali.dwm.render.ConsoleControlHud;
import com.adamkali.dwm.render.ConsoleHitboxDebugRenderer;
import com.adamkali.dwm.render.FirstDoctorConsoleSpecialRenderer;
import com.adamkali.dwm.render.TardisBlockSpecialRenderer;
import com.adamkali.dwm.render.TardisCompactScannerSpecialRenderer;
import com.adamkali.dwm.render.TardisFullScannerSpecialRenderer;
import com.adamkali.dwm.render.TardisGlobeSpecialRenderer;
import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.adamkali.dwm.render.portal.PortalPerfDebugHud;
import com.adamkali.dwm.render.portal.PortalPerfDebugLog;
import com.adamkali.dwm.render.portal.PortalSupport;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.sound.TardisHumController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_full_scanner"),
                TardisFullScannerSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_compact_scanner"),
                TardisCompactScannerSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_globe"),
                TardisGlobeSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "first_doctor_console"),
                FirstDoctorConsoleSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_block"),
                TardisBlockSpecialRenderer.Unbaked.MAP_CODEC);
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
        // ShaderManager compiles RenderPipelines.getStaticPipelines() during the first
        // resource reload, which is after client init. Register before that snapshot.
        PortalDoorRenderer.ensurePipelineRegistered();
        PortalSupport.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> PortalSceneStore.clientTick());
    }
}
