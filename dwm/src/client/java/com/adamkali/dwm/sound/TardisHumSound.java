package com.adamkali.dwm.sound;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Looping ambient mechanical hum while the local player is in {@code dwm:tardis}.
 */
public class TardisHumSound extends AbstractTickableSoundInstance {
    private static final float HUM_VOLUME = 0.25f;

    private final LocalPlayer player;

    public TardisHumSound(LocalPlayer player) {
        super(DWMSounds.TARDIS_HUM, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = HUM_VOLUME;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || !TardisDimensions.isTardisWorld(this.player.level())) {
            this.stop();
        }
    }
}
