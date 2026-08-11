package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public final class TardisDimensions {
    public static final Identifier DIMENSION_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis");
    public static final ResourceKey<Level> TARDIS_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, DIMENSION_ID);
    public static final Identifier CONSOLE_ROOM_STRUCTURE_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "first_doctor_console_room");

    /** Minimum doorSwing (0–1) required before exterior collision teleports the player. */
    public static final float ENTRY_DOOR_SWING_THRESHOLD = 0.9f;

    /** Minimum doorSwing (0–1) before exterior BOTI preview appears (lower than entry). */
    public static final float BOTI_DOOR_SWING_THRESHOLD = 0.15f;

    public static boolean isTardisWorld(ResourceKey<Level> worldKey) {
        return TARDIS_WORLD_KEY.equals(worldKey);
    }

    public static boolean isTardisWorld(Level world) {
        return world != null && isTardisWorld(world.dimension());
    }

    private TardisDimensions() {
    }
}
