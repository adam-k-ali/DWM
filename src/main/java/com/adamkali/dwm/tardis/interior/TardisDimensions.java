package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class TardisDimensions {
    public static final Identifier DIMENSION_ID = Identifier.of(DWMReference.MOD_ID, "tardis");
    public static final RegistryKey<World> TARDIS_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, DIMENSION_ID);
    public static final Identifier CONSOLE_ROOM_STRUCTURE_ID = Identifier.of(DWMReference.MOD_ID, "first_doctor_console_room");

    /** Minimum doorSwing (0–1) required before exterior collision teleports the player. */
    public static final float ENTRY_DOOR_SWING_THRESHOLD = 0.9f;

    private TardisDimensions() {
    }
}
