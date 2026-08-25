package com.adamkali.dwm.sound;

import com.adamkali.dwm.network.TravelAudioS2CPayload;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Looping dematerialise, materialise, or in-flight travel SFX. Positional at an exterior/landing site,
 * or relative (no attenuation) for interior listeners.
 */
public class TardisTravelLoopSound extends AbstractTickableSoundInstance {
    private static final float VOLUME = 0.85f;
    private static final float FLIGHT_VOLUME = 0.55f;

    private boolean finished;

    public TardisTravelLoopSound(SoundEvent event, BlockPos pos, boolean relative) {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = event == DWMSounds.TARDIS_FLIGHT_LOOP ? FLIGHT_VOLUME : VOLUME;
        this.relative = relative;
        if (relative) {
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
        } else {
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.x = pos.getX() + 0.5;
            this.y = pos.getY() + 0.5;
            this.z = pos.getZ() + 0.5;
        }
    }

    public static SoundEvent eventForAction(byte action) {
        return switch (action) {
            case TravelAudioS2CPayload.START_DEMAT -> DWMSounds.TARDIS_DEMATERIALISE_LOOP;
            case TravelAudioS2CPayload.START_MAT -> DWMSounds.TARDIS_MATERIALISE_LOOP;
            case TravelAudioS2CPayload.START_FLIGHT -> DWMSounds.TARDIS_FLIGHT_LOOP;
            default -> DWMSounds.TARDIS_DEMATERIALISE_LOOP;
        };
    }

    @Override
    public void tick() {
        if (finished) {
            this.stop();
        }
    }

    public void requestStop() {
        finished = true;
        this.stop();
    }
}
