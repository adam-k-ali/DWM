package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiInteriorSyncService;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;

public class ServerPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(OpenTardisChameleonScreen.ID, OpenTardisChameleonScreen.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncBotiInteriorS2CPayload.ID, SyncBotiInteriorS2CPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestBotiInteriorC2SPayload.ID, RequestBotiInteriorC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateTardisChameleonC2SPayload.ID, (payload, context) -> {
            safelyHandleChameleonUpdate(payload, context.player().getName().getString());
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestBotiInteriorC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleBotiRequest(payload, context.player()));
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
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Rejected chameleon update with invalid variant {} from {}", payload.variantId(), playerName);
            return false;
        }
    }

    static boolean safelyHandleBotiRequest(RequestBotiInteriorC2SPayload payload, net.minecraft.server.network.ServerPlayerEntity player) {
        if (payload == null || payload.tardisId() == null) {
            LOGGER.debug("Rejected BOTI request from {}: null tardisId", player.getName().getString());
            return false;
        }
        BotiPlotIndex.register(payload.tardisId());
        return BotiInteriorSyncService.sendToPlayer(player, payload.tardisId());
    }
}
