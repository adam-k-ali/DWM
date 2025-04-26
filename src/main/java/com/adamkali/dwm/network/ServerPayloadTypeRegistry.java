package com.adamkali.dwm.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ServerPayloadTypeRegistry {

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(OpenTardisChameleonScreen.ID, OpenTardisChameleonScreen.CODEC);

//        PayloadTypeRegistry.playC2S().register(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);
    }
}
