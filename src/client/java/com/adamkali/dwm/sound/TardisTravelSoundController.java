package com.adamkali.dwm.sound;

import com.adamkali.dwm.network.TravelAudioS2CPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Starts and stops {@link TardisTravelLoopSound} instances from S2C travel audio cues.
 */
public final class TardisTravelSoundController {
    private static final Map<UUID, TardisTravelLoopSound> ACTIVE = new ConcurrentHashMap<>();

    private TardisTravelSoundController() {
    }

    public static void handle(TravelAudioS2CPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> apply(client, payload));
    }

    public static void stopAll() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            for (Iterator<Map.Entry<UUID, TardisTravelLoopSound>> it = ACTIVE.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, TardisTravelLoopSound> entry = it.next();
                stopSound(client, entry.getValue());
                it.remove();
            }
        });
    }

    private static void apply(MinecraftClient client, TravelAudioS2CPayload payload) {
        if (payload.action() == TravelAudioS2CPayload.STOP) {
            TardisTravelLoopSound existing = ACTIVE.remove(payload.tardisId());
            stopSound(client, existing);
            return;
        }
        if (payload.action() != TravelAudioS2CPayload.START_DEMAT
                && payload.action() != TravelAudioS2CPayload.START_MAT) {
            return;
        }
        if (client.world == null
                || !client.world.getRegistryKey().getValue().equals(payload.dimensionId())) {
            return;
        }

        TardisTravelLoopSound previous = ACTIVE.remove(payload.tardisId());
        stopSound(client, previous);

        BlockPos pos = payload.pos();
        TardisTravelLoopSound next = new TardisTravelLoopSound(
                TardisTravelLoopSound.eventForAction(payload.action()),
                pos,
                payload.relative()
        );
        ACTIVE.put(payload.tardisId(), next);
        client.getSoundManager().play(next);
    }

    private static void stopSound(MinecraftClient client, TardisTravelLoopSound sound) {
        if (sound == null) {
            return;
        }
        sound.requestStop();
        if (client != null) {
            client.getSoundManager().stop(sound);
        }
    }
}
