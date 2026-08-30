package com.adamkali.dwm.network;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.gui.PlayerLocatorScreen;
import com.adamkali.dwm.gui.TardisChameleonGui;
import com.adamkali.dwm.gui.WaypointScreen;
import com.adamkali.dwm.render.SonicPingClientFx;
import com.adamkali.dwm.render.SonicScanHud;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalRenderTarget;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.sound.TardisTravelSoundController;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;

public class ClientPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(OpenTardisChameleonScreen.ID, ClientPayloadTypeRegistry::openTardisChameleonScreen);
        ClientPlayNetworking.registerGlobalReceiver(OpenWaypointScreen.ID, ClientPayloadTypeRegistry::openWaypointScreen);
        ClientPlayNetworking.registerGlobalReceiver(OpenPlayerLocatorScreen.ID, ClientPayloadTypeRegistry::openPlayerLocatorScreen);
        ClientPlayNetworking.registerGlobalReceiver(SyncPortalMetaS2CPayload.ID, ClientPayloadTypeRegistry::syncPortalMeta);
        ClientPlayNetworking.registerGlobalReceiver(SyncPortalChunkS2CPayload.ID, ClientPayloadTypeRegistry::syncPortalChunk);
        ClientPlayNetworking.registerGlobalReceiver(UnloadPortalChunkS2CPayload.ID, ClientPayloadTypeRegistry::unloadPortalChunk);
        ClientPlayNetworking.registerGlobalReceiver(SyncPortalEntitySpawnS2CPayload.ID, ClientPayloadTypeRegistry::spawnPortalEntity);
        ClientPlayNetworking.registerGlobalReceiver(SyncPortalEntityUpdateS2CPayload.ID, ClientPayloadTypeRegistry::updatePortalEntity);
        ClientPlayNetworking.registerGlobalReceiver(SyncPortalEntityRemoveS2CPayload.ID, ClientPayloadTypeRegistry::removePortalEntity);
        ClientPlayNetworking.registerGlobalReceiver(SyncPortalPerfS2CPayload.ID, ClientPayloadTypeRegistry::syncPortalPerf);
        ClientPlayNetworking.registerGlobalReceiver(TravelAudioS2CPayload.ID, ClientPayloadTypeRegistry::travelAudio);
        ClientPlayNetworking.registerGlobalReceiver(SonicPingRevealS2CPayload.ID, ClientPayloadTypeRegistry::sonicPingReveal);
        ClientPlayNetworking.registerGlobalReceiver(SonicScanS2CPayload.ID, ClientPayloadTypeRegistry::sonicScan);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PortalSceneStore.invalidateAll();
            PortalRenderTarget.closeGlobal();
            TardisTravelSoundController.stopAll();
            TardisDataLoader.clearCache();
            SonicPingClientFx.clear();
            SonicScanHud.clear();
        });
    }

    private static void openTardisChameleonScreen(OpenTardisChameleonScreen payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.client().level == null) {
                LOGGER.warn("Received OpenTardisChameleonScreen payload but client or world is null");
                return;
            }
            ClientTardis clientTardis = new ClientTardis(payload.tardisId());
            context.client().setScreenAndShow(new TardisChameleonGui(clientTardis));
        });
    }

    private static void openWaypointScreen(OpenWaypointScreen payload, ClientPlayNetworking.Context context) {
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

    private static void openPlayerLocatorScreen(OpenPlayerLocatorScreen payload, ClientPlayNetworking.Context context) {
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

    private static void syncPortalMeta(SyncPortalMetaS2CPayload payload, ClientPlayNetworking.Context context) {
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

    private static void syncPortalChunk(SyncPortalChunkS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> PortalSceneStore.applyChunk(payload));
    }

    private static void unloadPortalChunk(UnloadPortalChunkS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> PortalSceneStore.unloadChunk(payload));
    }

    private static void spawnPortalEntity(SyncPortalEntitySpawnS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> PortalSceneStore.applyEntitySpawn(payload));
    }

    private static void updatePortalEntity(SyncPortalEntityUpdateS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> PortalSceneStore.applyEntityUpdate(payload));
    }

    private static void removePortalEntity(SyncPortalEntityRemoveS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> PortalSceneStore.removeEntity(payload));
    }

    private static void syncPortalPerf(SyncPortalPerfS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> PortalPerfStats.applyServerDiag(payload));
    }

    private static void travelAudio(TravelAudioS2CPayload payload, ClientPlayNetworking.Context context) {
        TardisTravelSoundController.handle(payload);
    }

    private static void sonicPingReveal(SonicPingRevealS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var client = context.client();
            if (client.level == null) {
                return;
            }
            SonicPingClientFx.begin(
                    payload.tardisId(), payload.pos(), client.level.getGameTime());
            SonicPingClientFx.playCue(client.level, payload.pos());
        });
    }

    private static void sonicScan(SonicScanS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> SonicScanHud.show(payload.snapshot()));
    }
}
