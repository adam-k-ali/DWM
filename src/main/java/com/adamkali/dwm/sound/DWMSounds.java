package com.adamkali.dwm.sound;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class DWMSounds {
    public static final SoundEvent SONIC_SCREWDRIVER = register("sonic_screwdriver");

    public static void initialize() {
    }

    private static SoundEvent register(String id) {
        Identifier identifier = Identifier.of(DWMReference.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
}
