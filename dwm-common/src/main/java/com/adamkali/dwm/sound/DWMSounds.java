package com.adamkali.dwm.sound;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class DWMSounds {
    public static final SoundEvent SONIC_SCREWDRIVER = register("sonic_screwdriver");
    public static final SoundEvent TARDIS_DOOR_CLOSE = register("tardis_door_close");
    public static final SoundEvent TARDIS_DOOR_OPEN = register("tardis_door_open");
    public static final SoundEvent TARDIS_HUM = register("tardis_hum");
    public static final SoundEvent TARDIS_DEMATERIALISE_LOOP = register("tardis_dematerialise_loop");
    public static final SoundEvent TARDIS_MATERIALISE_LOOP = register("tardis_materialise_loop");
    public static final SoundEvent TARDIS_FLIGHT_LOOP = register("tardis_flight_loop");
    public static final SoundEvent TARDIS_MATERIALISE_THUD = register("tardis_materialise_thud");
    public static final SoundEvent BROAKIR_AMBIENT = register("entity.broakir.ambient");
    public static final SoundEvent BROAKIR_HURT = register("entity.broakir.hurt");
    public static final SoundEvent BROAKIR_DEATH = register("entity.broakir.death");
    public static final SoundEvent BROAKIR_STEP = register("entity.broakir.step");
    public static final SoundEvent FLUTTERWING_AMBIENT = register("entity.flutterwing.ambient");
    public static final SoundEvent FLUTTERWING_HURT = register("entity.flutterwing.hurt");
    public static final SoundEvent FLUTTERWING_DEATH = register("entity.flutterwing.death");

    public static void initialize() {
    }

    private static SoundEvent register(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }
}
