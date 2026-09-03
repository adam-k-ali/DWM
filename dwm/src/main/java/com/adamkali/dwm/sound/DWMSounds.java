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
    public static final SoundEvent MEWING_DOG_AMBIENT = register("entity.mewing_dog.ambient");
    public static final SoundEvent MEWING_DOG_HURT = register("entity.mewing_dog.hurt");
    public static final SoundEvent MEWING_DOG_DEATH = register("entity.mewing_dog.death");
    public static final SoundEvent MEWING_DOG_STEP = register("entity.mewing_dog.step");
    public static final SoundEvent TIME_LORD_AMBIENT = register("entity.time_lord.ambient");
    public static final SoundEvent TIME_LORD_HURT = register("entity.time_lord.hurt");
    public static final SoundEvent TIME_LORD_DEATH = register("entity.time_lord.death");
    public static final SoundEvent DALEK_AMBIENT = register("entity.dalek.ambient");
    public static final SoundEvent DALEK_HURT = register("entity.dalek.hurt");
    public static final SoundEvent DALEK_DEATH = register("entity.dalek.death");
    public static final SoundEvent DALEK_SHOOT = register("entity.dalek.shoot");

    public static void initialize() {
    }

    private static SoundEvent register(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }
}
