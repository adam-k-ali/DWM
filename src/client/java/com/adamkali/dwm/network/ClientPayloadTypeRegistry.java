package com.adamkali.dwm.network;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.gui.TardisChameleonGui;
import com.adamkali.dwm.render.boti.BotiInteriorMeshCache;
import com.adamkali.dwm.render.soto.SotoExteriorMeshCache;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;

public class ClientPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(OpenTardisChameleonScreen.ID, ClientPayloadTypeRegistry::openTardisChameleonScreen);
        ClientPlayNetworking.registerGlobalReceiver(SyncBotiInteriorS2CPayload.ID, ClientPayloadTypeRegistry::syncBotiInterior);
        ClientPlayNetworking.registerGlobalReceiver(SyncSotoExteriorS2CPayload.ID, ClientPayloadTypeRegistry::syncSotoExterior);
        ClientPlayNetworking.registerGlobalReceiver(SyncSotoExteriorChunkS2CPayload.ID, ClientPayloadTypeRegistry::syncSotoChunk);
        ClientPlayNetworking.registerGlobalReceiver(UnloadSotoExteriorChunkS2CPayload.ID, ClientPayloadTypeRegistry::unloadSotoChunk);
        ClientPlayNetworking.registerGlobalReceiver(SyncSotoExteriorEntitySpawnS2CPayload.ID, ClientPayloadTypeRegistry::spawnSotoEntity);
        ClientPlayNetworking.registerGlobalReceiver(SyncSotoExteriorEntityUpdateS2CPayload.ID, ClientPayloadTypeRegistry::updateSotoEntity);
        ClientPlayNetworking.registerGlobalReceiver(SyncSotoExteriorEntityRemoveS2CPayload.ID, ClientPayloadTypeRegistry::removeSotoEntity);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            BotiInteriorMeshCache.invalidateAll();
            SotoExteriorMeshCache.invalidateAll();
            SotoGhostExterior.invalidateAll();
        });
    }

    private static void openTardisChameleonScreen(OpenTardisChameleonScreen payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.client().world == null) {
                LOGGER.warn("Received OpenTardisChameleonScreen payload but client or world is null");
                return;
            }
            ClientTardis clientTardis = new ClientTardis(payload.tardisId());
            context.client().setScreen(new TardisChameleonGui(clientTardis));
        });
    }

    private static void syncBotiInterior(SyncBotiInteriorS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            BotiInteriorMeshCache.applySnapshot(
                    payload.tardisId(),
                    payload.revision(),
                    payload.toBlockMap(),
                    payload.toBlockEntityMap(),
                    payload.toEntityList()
            );
        });
    }

    private static void syncSotoExterior(SyncSotoExteriorS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            SotoExteriorMeshCache.applySnapshot(
                    payload.tardisId(),
                    payload.revision(),
                    payload.toBlockMap(),
                    payload.toBlockEntityMap(),
                    payload.toEntityList(),
                    payload.variant(),
                    payload.doorSwing(),
                    payload.isOpen(),
                    payload.exteriorRotation(),
                    payload.atmosphere()
            );
        });
    }

    private static void syncSotoChunk(SyncSotoExteriorChunkS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> SotoGhostExterior.applyChunk(payload));
    }

    private static void unloadSotoChunk(UnloadSotoExteriorChunkS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> SotoGhostExterior.unloadChunk(payload.tardisId(), payload.chunkX(), payload.chunkZ()));
    }

    private static void spawnSotoEntity(SyncSotoExteriorEntitySpawnS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> SotoGhostExterior.applyEntitySpawn(payload));
    }

    private static void updateSotoEntity(SyncSotoExteriorEntityUpdateS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> SotoGhostExterior.applyEntityUpdate(payload));
    }

    private static void removeSotoEntity(SyncSotoExteriorEntityRemoveS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> SotoGhostExterior.removeEntity(payload.tardisId(), payload.entityUuid()));
    }
}
