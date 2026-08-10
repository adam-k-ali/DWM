package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class DWMConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ASH = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash")
    );

    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_ASH = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash")
    );

    public static final ResourceKey<ConfiguredFeature<?, ?>> CARDINAL = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal")
    );

    private DWMConfiguredFeatures() {
    }
}
