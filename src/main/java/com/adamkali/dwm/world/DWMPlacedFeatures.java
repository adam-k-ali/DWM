package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class DWMPlacedFeatures {
    public static final RegistryKey<PlacedFeature> ASH_PLAINS = key("ash_plains");
    public static final RegistryKey<PlacedFeature> ASH_FOREST = key("ash_forest");
    public static final RegistryKey<PlacedFeature> DARK_ASH_FOREST = key("dark_ash_forest");
    public static final RegistryKey<PlacedFeature> CARDINAL_FOREST = key("cardinal_forest");

    private static RegistryKey<PlacedFeature> key(String path) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(DWMReference.MOD_ID, path));
    }

    private DWMPlacedFeatures() {
    }
}
