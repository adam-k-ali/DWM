package com.adamkali.dwm.network;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.gui.TardisChameleonGui;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import org.slf4j.Logger;

public class ClientPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(OpenTardisChameleonScreen.ID, ClientPayloadTypeRegistry::openTardisChameleonScreen);
    }

    private static void openTardisChameleonScreen(OpenTardisChameleonScreen payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.client().world == null) {
                LOGGER.warn("Received OpenTardisChameleonScreen payload but client or world is null");
                return;
            }
            BlockEntity blockEntity = context.client().world.getBlockEntity(payload.tardisPosition().pos());
            if (blockEntity instanceof TardisBlockEntity tardis) {
                ClientTardis clientTardis = new ClientTardis(tardis);
                context.client().setScreen(new TardisChameleonGui(clientTardis));
            } else {
                LOGGER.warn("Received OpenTardisChameleonScreen payload but block entity is not a TardisBlockEntity");
            }
        });
    }
}
