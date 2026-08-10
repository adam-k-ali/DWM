package com.adamkali.dwm.sound;

import com.adamkali.dwm.network.TravelAudioS2CPayload;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Starts and stops {@link TardisTravelLoopSound} instances from S2C travel audio cues.
 */
public final class TardisTravelSoundController {
    private static final Map<UUID, TardisTravelLoopSound> ACTIVE = new ConcurrentHashMap<>();

    private TardisTravelSoundController() {
    }

    public static void handle(TravelAudioS2CPayload payload) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> apply(client, payload));
    }

    public static void stopAll() {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            for (Iterator<Map.Entry<UUID, TardisTravelLoopSound>> it = ACTIVE.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, TardisTravelLoopSound> entry = it.next();
                stopSound(client, entry.getValue());
                it.remove();
            }
        });
    }

    private static void apply(Minecraft client, TravelAudioS2CPayload payload) {
        if (payload.action() == TravelAudioS2CPayload.STOP) {
            TardisTravelLoopSound existing = ACTIVE.remove(payload.tardisId());
            stopSound(client, existing);
            return;
        }
        if (payload.action() != TravelAudioS2CPayload.START_DEMAT
                && payload.action() != TravelAudioS2CPayload.START_MAT
                && payload.action() != TravelAudioS2CPayload.START_FLIGHT) {
            return;
        }
        if (client.level == null
                || !client.level.dimension().identifier().equals(payload.dimensionId())) {
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

    private static void stopSound(Minecraft client, TardisTravelLoopSound sound) {
        if (sound == null) {
            return;
        }
        sound.requestStop();
        if (client != null) {
            client.getSoundManager().stop(sound);
        }
    }
}
