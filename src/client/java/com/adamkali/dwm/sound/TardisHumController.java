package com.adamkali.dwm.sound;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Starts and stops {@link TardisHumSound} based on whether the local player is in the TARDIS dimension.
 */
public final class TardisHumController {
    private static TardisHumSound current;

    private TardisHumController() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(TardisHumController::onEndTick);
    }

    private static void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        boolean inTardis = player != null && TardisDimensions.isTardisWorld(player.getWorld());

        if (inTardis) {
            if (current == null || current.isDone()) {
                current = new TardisHumSound(player);
                client.getSoundManager().play(current);
            }
        } else if (current != null) {
            if (!current.isDone()) {
                client.getSoundManager().stop(current);
            }
            current = null;
        }
    }
}
