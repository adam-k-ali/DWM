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
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
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

        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.AZBANTIUM_ORE,
                configured.getOrThrow(DWMConfiguredFeatures.AZBANTIUM_ORE),
                CountPlacement.of(8),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32)),
                BiomeFilter.biome()
        );

        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.ZEITON_ORE,
                configured.getOrThrow(DWMConfiguredFeatures.ZEITON_ORE),
                CountPlacement.of(8),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.ZEITON_ORE_OVERWORLD,
                configured.getOrThrow(DWMConfiguredFeatures.ZEITON_ORE_OVERWORLD),
                CountPlacement.of(UniformInt.of(1, 2)),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(48)),
                BiomeFilter.biome()
        );

        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_COAL_ORE_UPPER,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_COAL_ORE),
                CountPlacement.of(30),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.belowTop(0)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_COAL_ORE_LOWER,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_COAL_ORE_BURIED),
                CountPlacement.of(20),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192)),
                BiomeFilter.biome()
        );

        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_IRON_ORE_UPPER,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_IRON_ORE),
                CountPlacement.of(90),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_IRON_ORE_MIDDLE,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_IRON_ORE),
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_IRON_ORE_SMALL,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_IRON_ORE_SMALL),
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.absolute(72)),
                BiomeFilter.biome()
        );

        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_GOLD_ORE,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_GOLD_ORE_BURIED),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_GOLD_ORE_LOWER,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_GOLD_ORE_BURIED),
                CountPlacement.of(UniformInt.of(0, 1)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48)),
                BiomeFilter.biome()
        );

        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_SMALL),
                CountPlacement.of(7),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_MEDIUM,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_MEDIUM),
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-4)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_LARGE,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_LARGE),
                RarityFilter.onAverageOnceEvery(9),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_BURIED,
                configured.getOrThrow(DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_BURIED),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                BiomeFilter.biome()
        );

        registerSaplingFreeTree(
                registerable,
                DWMPlacedFeatures.PETRIFIED_JUNGLE_TREES,
                configured.getOrThrow(DWMConfiguredFeatures.PETRIFIED_TREE),
                PlacementUtils.countExtra(12, 0.1F, 1)
        );
        registerSaplingFreeTree(
                registerable,
                DWMPlacedFeatures.PETRIFIED_JUNGLE_SNAGS,
                configured.getOrThrow(DWMConfiguredFeatures.PETRIFIED_SNAG),
                PlacementUtils.countExtra(8, 0.1F, 1)
        );
        PlacementUtils.register(
                registerable,
                DWMPlacedFeatures.FALLEN_PETRIFIED_JUNGLE_TREES,
                configured.getOrThrow(DWMConfiguredFeatures.FALLEN_PETRIFIED_TREE),
                RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome()
        );
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

    /** Tree placement without a sapling would-survive check (petrified wood has no saplings). */
    private static void registerSaplingFreeTree(
            BootstrapContext<PlacedFeature> registerable,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> feature,
            PlacementModifier countModifier
    ) {
        PlacementUtils.register(
                registerable,
                key,
                feature,
                VegetationPlacements.treePlacement(countModifier)
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
