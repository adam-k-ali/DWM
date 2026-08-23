package com.adamkali.dwm;

import com.adamkali.dwm.client.DWMEntityRenderers;
import com.adamkali.dwm.network.ClientPayloadTypeRegistry;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.render.ConsoleControlHud;
import com.adamkali.dwm.render.ConsoleHitboxDebugRenderer;
import com.adamkali.dwm.render.TardisCompactScannerSpecialRenderer;
import com.adamkali.dwm.render.TardisFullScannerSpecialRenderer;
import com.adamkali.dwm.render.TardisGlobeSpecialRenderer;
import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.adamkali.dwm.render.portal.PortalPerfDebugHud;
import com.adamkali.dwm.render.portal.PortalPerfDebugLog;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.render.portal.PortalSupport;
import com.adamkali.dwm.sound.TardisHumController;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;

/**
 * Shared client initialization. Loader entrypoints must install
 * {@link com.adamkali.dwm.platform.DwmClientPlatform} via {@link DwmClientServices#set}
 * before calling {@link #init()}.
 */
public final class DwmCommonClient {
    private DwmCommonClient() {
    }

    public static void init() {
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_full_scanner"),
                TardisFullScannerSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_compact_scanner"),
                TardisCompactScannerSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_globe"),
                TardisGlobeSpecialRenderer.Unbaked.MAP_CODEC);
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
        DwmClientServices.get().registerEndClientTick(client -> PortalSceneStore.clientTick());
    }
}
