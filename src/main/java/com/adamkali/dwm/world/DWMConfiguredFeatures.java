package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public final class DWMConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> ASH = RegistryKey.of(
            RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of(DWMReference.MOD_ID, "ash")
    );

    public static final RegistryKey<ConfiguredFeature<?, ?>> DARK_ASH = RegistryKey.of(
            RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of(DWMReference.MOD_ID, "dark_ash")
    );

    public static final RegistryKey<ConfiguredFeature<?, ?>> CARDINAL = RegistryKey.of(
            RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of(DWMReference.MOD_ID, "cardinal")
    );

    private DWMConfiguredFeatures() {
    }
}
