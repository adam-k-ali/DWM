package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class DWMPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ASH_PLAINS = key("ash_plains");
    public static final ResourceKey<PlacedFeature> ASH_FOREST = key("ash_forest");
    public static final ResourceKey<PlacedFeature> DARK_ASH_FOREST = key("dark_ash_forest");
    public static final ResourceKey<PlacedFeature> CARDINAL_FOREST = key("cardinal_forest");
    public static final ResourceKey<PlacedFeature> GALLIFREY_FLOWERS_PLAINS = key("gallifrey_flowers_plains");
    public static final ResourceKey<PlacedFeature> GALLIFREY_FLOWERS_FOREST = key("gallifrey_flowers_forest");
    public static final ResourceKey<PlacedFeature> SACCHARINE_CANE_WASTES = key("saccharine_cane_wastes");
    public static final ResourceKey<PlacedFeature> SACCHARINE_CANE_BADLANDS = key("saccharine_cane_badlands");
    public static final ResourceKey<PlacedFeature> AZBANTIUM_ORE = key("azbantium_ore");

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMPlacedFeatures() {
    }
}
