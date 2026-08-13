package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

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

        Holder<ConfiguredFeature<?, ?>> flowers = configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_FLOWERS);
        registerFlowerPatch(registerable, DWMPlacedFeatures.GALLIFREY_FLOWERS_PLAINS, flowers, 16);
        registerFlowerPatch(registerable, DWMPlacedFeatures.GALLIFREY_FLOWERS_FOREST, flowers, 32);

        Holder<ConfiguredFeature<?, ?>> cane = configured.getOrThrow(DWMConfiguredFeatures.SACCHARINE_CANE);
        registerSaccharineCane(registerable, DWMPlacedFeatures.SACCHARINE_CANE_WASTES, cane, 4);
        registerSaccharineCane(registerable, DWMPlacedFeatures.SACCHARINE_CANE_BADLANDS, cane, 5);
    }

    private static void registerTree(
            BootstrapContext<PlacedFeature> registerable,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> feature,
            PlacementModifier countModifier,
            net.minecraft.world.level.block.Block sapling
    ) {
        PlacementUtils.register(
                registerable,
                key,
                feature,
                VegetationPlacements.treePlacement(countModifier, sapling)
        );
    }

    private static void registerFlowerPatch(
            BootstrapContext<PlacedFeature> registerable,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> feature,
            int rarity
    ) {
        PlacementUtils.register(
                registerable,
                key,
                feature,
                RarityFilter.onAverageOnceEvery(rarity),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                CountPlacement.of(64),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)
        );
    }

    private static void registerSaccharineCane(
            BootstrapContext<PlacedFeature> registerable,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> feature,
            int rarity
    ) {
        PlacementUtils.register(
                registerable,
                key,
                feature,
                RarityFilter.onAverageOnceEvery(rarity),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                CountPlacement.of(20),
                RandomOffsetPlacement.ofTriangle(4, 0),
                BlockPredicateFilter.forPredicate(
                        BlockPredicate.allOf(
                                BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                BlockPredicate.anyOf(
                                        BlockPredicate.matchesTag(Direction.DOWN.getUnitVec3i(), BlockTags.DIRT),
                                        BlockPredicate.matchesTag(Direction.DOWN.getUnitVec3i(), BlockTags.SAND)
                                )
                        )
                )
        );
    }
}
