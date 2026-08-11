package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;

public class ServerPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(OpenTardisChameleonScreen.ID, OpenTardisChameleonScreen.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalMetaS2CPayload.ID, SyncPortalMetaS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalChunkS2CPayload.ID, SyncPortalChunkS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UnloadPortalChunkS2CPayload.ID, UnloadPortalChunkS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalEntitySpawnS2CPayload.ID, SyncPortalEntitySpawnS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalEntityUpdateS2CPayload.ID, SyncPortalEntityUpdateS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalEntityRemoveS2CPayload.ID, SyncPortalEntityRemoveS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TravelAudioS2CPayload.ID, TravelAudioS2CPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestPortalStreamC2SPayload.ID, RequestPortalStreamC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateTardisChameleonC2SPayload.ID, (payload, context) -> {
            safelyHandleChameleonUpdate(payload, context.player().getName().getString());
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestPortalStreamC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandlePortalStreamRequest(payload, context.player()));
        });
    }

    static boolean safelyHandleChameleonUpdate(UpdateTardisChameleonC2SPayload payload, String playerName) {
        try {
            TardisDataModel tardis = TardisDataLoader.get(payload.tardisId());
            if (tardis == null) {
                LOGGER.warn("Rejected chameleon update for unknown tardisId {} from {}", payload.tardisId(), playerName);
                return false;
            }

            TardisChameleonVariant variant = TardisChameleonVariant.fromId(payload.variantId());
            TardisLogic.setVariant(payload.tardisId(), variant);
            PortalStreamSyncService.setMetaChanged(payload.tardisId());
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Rejected chameleon update with invalid variant {} from {}", payload.variantId(), playerName);
            return false;
        }
    }

    static boolean safelyHandlePortalStreamRequest(RequestPortalStreamC2SPayload payload, net.minecraft.server.level.ServerPlayer player) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null || player == null) {
            LOGGER.debug("Rejected portal stream request: null payload, kind, tardisId, or player");
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(payload.tardisId());
        if (model != null) {
            if (payload.kind() == PortalStreamKind.SOTO) {
                SotoExteriorIndex.register(payload.tardisId(), model);
            } else {
                BotiPlotIndex.register(payload.tardisId());
            }
        }
        return PortalStreamSyncService.subscribe(player, payload.kind(), payload.tardisId());
    }
}
