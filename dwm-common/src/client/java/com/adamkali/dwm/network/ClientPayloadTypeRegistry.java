package com.adamkali.dwm.network;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.gui.PlayerLocatorScreen;
import com.adamkali.dwm.gui.TardisChameleonGui;
import com.adamkali.dwm.gui.WaypointScreen;
import com.adamkali.dwm.platform.DwmClientPlatform;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalRenderTarget;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.sound.TardisTravelSoundController;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class ClientPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        DwmClientPlatform platform = DwmClientServices.get();
        platform.registerClientboundHandler(OpenTardisChameleonScreen.ID, ClientPayloadTypeRegistry::openTardisChameleonScreen);
        platform.registerClientboundHandler(OpenWaypointScreen.ID, ClientPayloadTypeRegistry::openWaypointScreen);
        platform.registerClientboundHandler(OpenPlayerLocatorScreen.ID, ClientPayloadTypeRegistry::openPlayerLocatorScreen);
        platform.registerClientboundHandler(SyncPortalMetaS2CPayload.ID, ClientPayloadTypeRegistry::syncPortalMeta);
        platform.registerClientboundHandler(SyncPortalChunkS2CPayload.ID, ClientPayloadTypeRegistry::syncPortalChunk);
        platform.registerClientboundHandler(UnloadPortalChunkS2CPayload.ID, ClientPayloadTypeRegistry::unloadPortalChunk);
        platform.registerClientboundHandler(SyncPortalEntitySpawnS2CPayload.ID, ClientPayloadTypeRegistry::spawnPortalEntity);
        platform.registerClientboundHandler(SyncPortalEntityUpdateS2CPayload.ID, ClientPayloadTypeRegistry::updatePortalEntity);
        platform.registerClientboundHandler(SyncPortalEntityRemoveS2CPayload.ID, ClientPayloadTypeRegistry::removePortalEntity);
        platform.registerClientboundHandler(SyncPortalPerfS2CPayload.ID, ClientPayloadTypeRegistry::syncPortalPerf);
        platform.registerClientboundHandler(TravelAudioS2CPayload.ID, ClientPayloadTypeRegistry::travelAudio);
        platform.registerClientDisconnect((handler, client) -> {
            PortalSceneStore.invalidateAll();
            PortalRenderTarget.closeGlobal();
            TardisTravelSoundController.stopAll();
            TardisDataLoader.clearCache();
        });
    }

    private static void openTardisChameleonScreen(OpenTardisChameleonScreen payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> {
            if (context.client().level == null) {
                LOGGER.warn("Received OpenTardisChameleonScreen payload but client or world is null");
                return;
            }
            ClientTardis clientTardis = new ClientTardis(payload.tardisId());
            context.client().setScreenAndShow(new TardisChameleonGui(clientTardis));
        });
    }

    private static void openWaypointScreen(OpenWaypointScreen payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> {
            if (context.client().level == null) {
                LOGGER.warn("Received OpenWaypointScreen payload but client or world is null");
                return;
            }
            ClientTardis clientTardis = new ClientTardis(payload.tardisId());
            context.client().setScreenAndShow(new WaypointScreen(
                    clientTardis,
                    payload.waypoints(),
                    payload.canSave(),
                    payload.destinationWaypointId(),
                    payload.locationWaypointId(),
                    payload.exteriorLocation()
            ));
        });
    }

    private static void openPlayerLocatorScreen(OpenPlayerLocatorScreen payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> {
            if (context.client().level == null) {
                LOGGER.warn("Received OpenPlayerLocatorScreen payload but client or world is null");
                return;
            }
            ClientTardis clientTardis = new ClientTardis(payload.tardisId());
            context.client().setScreenAndShow(new PlayerLocatorScreen(
                    clientTardis,
                    payload.players(),
                    payload.selectedPlayerUuid()
            ));
        });
    }

    private static void syncPortalMeta(SyncPortalMetaS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> {
            PortalSceneStore.applyMeta(payload);
            // Remote clients have no save directory; seed the shared cache so exterior
            // tick/render can animate doors without hitting disk.
            TardisDataLoader.applyClientShell(
                    payload.tardisId(),
                    payload.variant(),
                    payload.doorSwing(),
                    payload.isOpen()
            );
        });
    }

    private static void syncPortalChunk(SyncPortalChunkS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> PortalSceneStore.applyChunk(payload));
    }

    private static void unloadPortalChunk(UnloadPortalChunkS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> PortalSceneStore.unloadChunk(payload));
    }

    private static void spawnPortalEntity(SyncPortalEntitySpawnS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> PortalSceneStore.applyEntitySpawn(payload));
    }

    private static void updatePortalEntity(SyncPortalEntityUpdateS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> PortalSceneStore.applyEntityUpdate(payload));
    }

    private static void removePortalEntity(SyncPortalEntityRemoveS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> PortalSceneStore.removeEntity(payload));
    }

    private static void syncPortalPerf(SyncPortalPerfS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        context.client().execute(() -> PortalPerfStats.applyServerDiag(payload));
    }

    private static void travelAudio(TravelAudioS2CPayload payload, DwmClientPlatform.ClientPlayContext context) {
        TardisTravelSoundController.handle(payload);
    }
}
