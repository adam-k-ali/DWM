package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class DWMPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ASH_PLAINS = key("ash_plains");
    public static final ResourceKey<PlacedFeature> ASH_FOREST = key("ash_forest");
    /** Village-piece tree: configured Ash tree with sapling survival filter only (no biome rarity). */
    public static final ResourceKey<PlacedFeature> ASH_VILLAGE = key("ash_village");
    public static final ResourceKey<PlacedFeature> DARK_ASH_FOREST = key("dark_ash_forest");
    public static final ResourceKey<PlacedFeature> CARDINAL_FOREST = key("cardinal_forest");
    public static final ResourceKey<PlacedFeature> GALLIFREY_FLOWERS_PLAINS = key("gallifrey_flowers_plains");
    public static final ResourceKey<PlacedFeature> GALLIFREY_FLOWERS_FOREST = key("gallifrey_flowers_forest");
    public static final ResourceKey<PlacedFeature> SACCHARINE_CANE_WASTES = key("saccharine_cane_wastes");
    public static final ResourceKey<PlacedFeature> SACCHARINE_CANE_BADLANDS = key("saccharine_cane_badlands");
    public static final ResourceKey<PlacedFeature> AZBANTIUM_ORE = key("azbantium_ore");

    public static final ResourceKey<PlacedFeature> GALLIFREY_COAL_ORE_UPPER = key("gallifrey_coal_ore_upper");
    public static final ResourceKey<PlacedFeature> GALLIFREY_COAL_ORE_LOWER = key("gallifrey_coal_ore_lower");
    public static final ResourceKey<PlacedFeature> GALLIFREY_IRON_ORE_UPPER = key("gallifrey_iron_ore_upper");
    public static final ResourceKey<PlacedFeature> GALLIFREY_IRON_ORE_MIDDLE = key("gallifrey_iron_ore_middle");
    public static final ResourceKey<PlacedFeature> GALLIFREY_IRON_ORE_SMALL = key("gallifrey_iron_ore_small");
    public static final ResourceKey<PlacedFeature> GALLIFREY_GOLD_ORE = key("gallifrey_gold_ore");
    public static final ResourceKey<PlacedFeature> GALLIFREY_GOLD_ORE_LOWER = key("gallifrey_gold_ore_lower");
    public static final ResourceKey<PlacedFeature> GALLIFREY_DIAMOND_ORE = key("gallifrey_diamond_ore");
    public static final ResourceKey<PlacedFeature> GALLIFREY_DIAMOND_ORE_MEDIUM = key("gallifrey_diamond_ore_medium");
    public static final ResourceKey<PlacedFeature> GALLIFREY_DIAMOND_ORE_LARGE = key("gallifrey_diamond_ore_large");
    public static final ResourceKey<PlacedFeature> GALLIFREY_DIAMOND_ORE_BURIED = key("gallifrey_diamond_ore_buried");

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMPlacedFeatures() {
    }
}
