package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Constants for the Gallifrey destination dimension ({@code dwm:gallifrey}).
 */
public final class GallifreyDimensions {
    public static final Identifier DIMENSION_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey");
    public static final ResourceKey<Level> GALLIFREY_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, DIMENSION_ID);

    public static boolean isGallifreyWorld(ResourceKey<Level> worldKey) {
        return GALLIFREY_WORLD_KEY.equals(worldKey);
    }

    public static boolean isGallifreyWorld(Level world) {
        return world != null && isGallifreyWorld(world.dimension());
    }

    private GallifreyDimensions() {
    }
}
