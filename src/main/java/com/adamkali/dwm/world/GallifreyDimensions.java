package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Constants for the Gallifrey destination dimension ({@code dwm:gallifrey}).
 */
public final class GallifreyDimensions {
    public static final Identifier DIMENSION_ID = Identifier.of(DWMReference.MOD_ID, "gallifrey");
    public static final RegistryKey<World> GALLIFREY_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, DIMENSION_ID);

    public static boolean isGallifreyWorld(RegistryKey<World> worldKey) {
        return GALLIFREY_WORLD_KEY.equals(worldKey);
    }

    public static boolean isGallifreyWorld(World world) {
        return world != null && isGallifreyWorld(world.getRegistryKey());
    }

    private GallifreyDimensions() {
    }
}
