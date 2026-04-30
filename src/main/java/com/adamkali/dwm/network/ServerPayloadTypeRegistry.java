package com.adamkali.dwm.network;

import com.adamkali.dwm.config.DWMConfig;
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

        PayloadTypeRegistry.playC2S().register(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateTardisChameleonC2SPayload.ID, (payload, context) -> {
            safelyHandleChameleonUpdate(payload, context.player().getName().getString());
        });
    }

    static boolean safelyHandleChameleonUpdate(UpdateTardisChameleonC2SPayload payload, String playerName) {
        try {
            if (!DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)) {
                LOGGER.warn("Rejected chameleon update while experimental feature is disabled from {}", playerName);
                return false;
            }
            if (payload == null || payload.tardisId() == null || payload.variantId() == null) {
                LOGGER.warn("Rejected malformed chameleon payload from {}", playerName);
                return false;
            }

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
}
