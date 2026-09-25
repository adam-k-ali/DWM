package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class DWMConfiguredFeatures {
    public static final ResourceKey<Feature> ASH = key("ash");
    public static final ResourceKey<Feature> DARK_ASH = key("dark_ash");
    public static final ResourceKey<Feature> CARDINAL = key("cardinal");
    public static final ResourceKey<Feature> GALLIFREY_FLOWERS = key("gallifrey_flowers");
    public static final ResourceKey<Feature> SACCHARINE_CANE = key("saccharine_cane");
    public static final ResourceKey<Feature> AZBANTIUM_ORE = key("azbantium_ore");
    public static final ResourceKey<Feature> ZEITON_ORE = key("zeiton_ore");
    public static final ResourceKey<Feature> ZEITON_ORE_OVERWORLD = key("zeiton_ore_overworld");
    public static final ResourceKey<Feature> DALEKANIUM_ORE = key("dalekanium_ore");

    public static final ResourceKey<Feature> GALLIFREY_COAL_ORE = key("gallifrey_coal_ore");
    public static final ResourceKey<Feature> GALLIFREY_COAL_ORE_BURIED = key("gallifrey_coal_ore_buried");
    public static final ResourceKey<Feature> GALLIFREY_IRON_ORE = key("gallifrey_iron_ore");
    public static final ResourceKey<Feature> GALLIFREY_IRON_ORE_SMALL = key("gallifrey_iron_ore_small");
    public static final ResourceKey<Feature> GALLIFREY_GOLD_ORE = key("gallifrey_gold_ore");
    public static final ResourceKey<Feature> GALLIFREY_GOLD_ORE_BURIED = key("gallifrey_gold_ore_buried");
    public static final ResourceKey<Feature> GALLIFREY_DIAMOND_ORE_SMALL = key("gallifrey_diamond_ore_small");
    public static final ResourceKey<Feature> GALLIFREY_DIAMOND_ORE_MEDIUM = key("gallifrey_diamond_ore_medium");
    public static final ResourceKey<Feature> GALLIFREY_DIAMOND_ORE_LARGE = key("gallifrey_diamond_ore_large");
    public static final ResourceKey<Feature> GALLIFREY_DIAMOND_ORE_BURIED = key("gallifrey_diamond_ore_buried");

    public static final ResourceKey<Feature> PETRIFIED_TREE = key("petrified_tree");
    public static final ResourceKey<Feature> PETRIFIED_SNAG = key("petrified_snag");
    public static final ResourceKey<Feature> FALLEN_PETRIFIED_TREE = key("fallen_petrified_tree");

    private static ResourceKey<Feature> key(String path) {
        return ResourceKey.create(
                Registries.FEATURE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path)
        );
    }

    private DWMConfiguredFeatures() {
    }
}
