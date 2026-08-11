package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class DWMPlacedFeatureBootstrap {
    private DWMPlacedFeatureBootstrap() {
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> registerable) {
        HolderGetter<ConfiguredFeature<?, ?>> configured =
                registerable.lookup(Registries.CONFIGURED_FEATURE);

        registerTree(
                registerable,
                DWMPlacedFeatures.ASH_PLAINS,
                configured.getOrThrow(DWMConfiguredFeatures.ASH),
                PlacementUtils.countExtra(0, 0.05F, 1),
                DWMBlocks.ASH_SAPLING
        );
        registerTree(
                registerable,
                DWMPlacedFeatures.ASH_FOREST,
                configured.getOrThrow(DWMConfiguredFeatures.ASH),
                PlacementUtils.countExtra(4, 0.1F, 1),
                DWMBlocks.ASH_SAPLING
        );
        registerTree(
                registerable,
                DWMPlacedFeatures.DARK_ASH_FOREST,
                configured.getOrThrow(DWMConfiguredFeatures.DARK_ASH),
                PlacementUtils.countExtra(3, 0.1F, 1),
                DWMBlocks.DARK_ASH_SAPLING
        );
        registerTree(
                registerable,
                DWMPlacedFeatures.CARDINAL_FOREST,
                configured.getOrThrow(DWMConfiguredFeatures.CARDINAL),
                PlacementUtils.countExtra(2, 0.1F, 1),
                DWMBlocks.CARDINAL_SAPLING
        );
    }

    private static void registerTree(
            BootstrapContext<PlacedFeature> registerable,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> feature,
            net.minecraft.world.level.levelgen.placement.PlacementModifier countModifier,
            net.minecraft.world.level.block.Block sapling
    ) {
        PlacementUtils.register(
                registerable,
                key,
                feature,
                VegetationPlacements.treePlacement(countModifier, sapling)
        );
    }
}
