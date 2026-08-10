package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public final class DWMPlacedFeatureBootstrap {
    private DWMPlacedFeatureBootstrap() {
    }

    public static void bootstrap(Registerable<PlacedFeature> registerable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> configured =
                registerable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        registerTree(
                registerable,
                DWMPlacedFeatures.ASH_PLAINS,
                configured.getOrThrow(DWMConfiguredFeatures.ASH),
                PlacedFeatures.createCountExtraModifier(0, 0.05F, 1),
                DWMBlocks.ASH_SAPLING
        );
        registerTree(
                registerable,
                DWMPlacedFeatures.ASH_FOREST,
                configured.getOrThrow(DWMConfiguredFeatures.ASH),
                PlacedFeatures.createCountExtraModifier(4, 0.1F, 1),
                DWMBlocks.ASH_SAPLING
        );
        registerTree(
                registerable,
                DWMPlacedFeatures.DARK_ASH_FOREST,
                configured.getOrThrow(DWMConfiguredFeatures.DARK_ASH),
                PlacedFeatures.createCountExtraModifier(3, 0.1F, 1),
                DWMBlocks.DARK_ASH_SAPLING
        );
        registerTree(
                registerable,
                DWMPlacedFeatures.CARDINAL_FOREST,
                configured.getOrThrow(DWMConfiguredFeatures.CARDINAL),
                PlacedFeatures.createCountExtraModifier(2, 0.1F, 1),
                DWMBlocks.CARDINAL_SAPLING
        );
    }

    private static void registerTree(
            Registerable<PlacedFeature> registerable,
            RegistryKey<PlacedFeature> key,
            RegistryEntry<ConfiguredFeature<?, ?>> feature,
            net.minecraft.world.gen.placementmodifier.PlacementModifier countModifier,
            net.minecraft.block.Block sapling
    ) {
        PlacedFeatures.register(
                registerable,
                key,
                feature,
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(countModifier, sapling)
        );
    }
}
