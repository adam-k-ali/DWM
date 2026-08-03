package com.adamkali.dwm.sound;

import com.adamkali.dwm.network.TravelAudioS2CPayload;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

/**
 * Looping dematerialise, materialise, or in-flight travel SFX. Positional at an exterior/landing site,
 * or relative (no attenuation) for interior listeners.
 */
public class TardisTravelLoopSound extends MovingSoundInstance {
    private static final float VOLUME = 0.85f;
    private static final float FLIGHT_VOLUME = 0.55f;

    private boolean finished;

    public TardisTravelLoopSound(SoundEvent event, BlockPos pos, boolean relative) {
        super(event, SoundCategory.BLOCKS, SoundInstance.createRandom());
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = event == DWMSounds.TARDIS_FLIGHT_LOOP ? FLIGHT_VOLUME : VOLUME;
        this.relative = relative;
        if (relative) {
            this.attenuationType = SoundInstance.AttenuationType.NONE;
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
        } else {
            this.attenuationType = SoundInstance.AttenuationType.LINEAR;
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
            this.setDone();
        }
    }

    public void requestStop() {
        finished = true;
        this.setDone();
    }
}
