package com.adamkali.dwm.sound;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

/**
 * Looping ambient mechanical hum while the local player is in {@code dwm:tardis}.
 */
public class TardisHumSound extends MovingSoundInstance {
    private static final float HUM_VOLUME = 0.25f;

    private final ClientPlayerEntity player;

    public TardisHumSound(ClientPlayerEntity player) {
        super(DWMSounds.TARDIS_HUM, SoundCategory.AMBIENT, SoundInstance.createRandom());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = HUM_VOLUME;
        this.relative = true;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || !TardisDimensions.isTardisWorld(this.player.getWorld())) {
            this.setDone();
        }
    }
}
