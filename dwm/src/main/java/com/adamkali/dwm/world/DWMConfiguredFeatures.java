package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class DWMConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ASH = key("ash");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_ASH = key("dark_ash");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CARDINAL = key("cardinal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_FLOWERS = key("gallifrey_flowers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SACCHARINE_CANE = key("saccharine_cane");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AZBANTIUM_ORE = key("azbantium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ZEITON_ORE = key("zeiton_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ZEITON_ORE_OVERWORLD = key("zeiton_ore_overworld");

    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_COAL_ORE = key("gallifrey_coal_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_COAL_ORE_BURIED = key("gallifrey_coal_ore_buried");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_IRON_ORE = key("gallifrey_iron_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_IRON_ORE_SMALL = key("gallifrey_iron_ore_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_GOLD_ORE = key("gallifrey_gold_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_GOLD_ORE_BURIED = key("gallifrey_gold_ore_buried");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_DIAMOND_ORE_SMALL = key("gallifrey_diamond_ore_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_DIAMOND_ORE_MEDIUM = key("gallifrey_diamond_ore_medium");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_DIAMOND_ORE_LARGE = key("gallifrey_diamond_ore_large");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREY_DIAMOND_ORE_BURIED = key("gallifrey_diamond_ore_buried");

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path)
        );
    }

    private DWMConfiguredFeatures() {
    }
}
