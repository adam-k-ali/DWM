package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Constants for the Skaro destination dimension ({@code dwm:skaro}).
 */
public final class SkaroDimensions {
    public static final Identifier DIMENSION_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro");
    public static final ResourceKey<Level> SKARO_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, DIMENSION_ID);

    public static boolean isSkaroWorld(ResourceKey<Level> worldKey) {
        return SKARO_WORLD_KEY.equals(worldKey);
    }

    public static boolean isSkaroWorld(Level world) {
        return world != null && isSkaroWorld(world.dimension());
    }

    private SkaroDimensions() {
    }
}
