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

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path)
        );
    }

    private DWMConfiguredFeatures() {
    }
}
