package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class DWMPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ASH_PLAINS = key("ash_plains");
    public static final ResourceKey<PlacedFeature> ASH_FOREST = key("ash_forest");
    public static final ResourceKey<PlacedFeature> DARK_ASH_FOREST = key("dark_ash_forest");
    public static final ResourceKey<PlacedFeature> CARDINAL_FOREST = key("cardinal_forest");

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMPlacedFeatures() {
    }
}
