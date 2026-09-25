package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlockTags;
import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BlockStateProviders;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.OptionalInt;

public final class DWMConfiguredFeatureBootstrap {
    private DWMConfiguredFeatureBootstrap() {
    }

    public static void bootstrap(BootstrapContext<Feature> registerable) {
        registerTree(registerable, DWMConfiguredFeatures.ASH, DWMBlocks.ASH_LOG, DWMBlocks.ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.DARK_ASH, DWMBlocks.DARK_ASH_LOG, DWMBlocks.DARK_ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.CARDINAL, DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LEAVES);

        WeightedList.Builder<BlockState> flowers = WeightedList.builder();
        flowers.add(DWMBlocks.FLOWER_OF_REMEMBRANCE.defaultBlockState(), 2);
        flowers.add(DWMBlocks.MOONLIGHT_BLOOM.defaultBlockState(), 1);
        registerable.register(
                DWMConfiguredFeatures.GALLIFREY_FLOWERS,
                new SimpleBlockFeature(new WeightedStateProvider(flowers))
        );

        registerable.register(
                DWMConfiguredFeatures.SACCHARINE_CANE,
                BlockColumnFeature.simple(
                        BiasedToBottomInt.of(2, 4),
                        BlockStateProvider.of(DWMBlocks.SACCHARINE_CANE)
                )
        );

        registerable.register(
                DWMConfiguredFeatures.AZBANTIUM_ORE,
                new OreFeature(
                        java.util.List.of(BlockReplacement.replace(
                                new TagMatchTest(DWMBlockTags.GALLIFREY_ORE_REPLACEABLES),
                                DWMBlocks.AZBANTIUM_ORE.defaultBlockState()
                        )),
                        9,
                        0.5F
                )
        );

        registerOre(registerable, DWMConfiguredFeatures.ZEITON_ORE, DWMBlocks.ZEITON_ORE, 6, 0.5F);
        registerOre(
                registerable,
                DWMConfiguredFeatures.ZEITON_ORE_OVERWORLD,
                DWMBlocks.ZEITON_ORE,
                DWMBlockTags.STONE_ORE_REPLACEABLES,
                3,
                0.5F
        );
        registerOre(
                registerable,
                DWMConfiguredFeatures.DALEKANIUM_ORE,
                DWMBlocks.DALEKANIUM_ORE,
                DWMBlockTags.STONE_ORE_REPLACEABLES,
                9,
                0.0F
        );

        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_COAL_ORE, DWMBlocks.GALLIFREY_COAL_ORE, 17, 0.0F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_COAL_ORE_BURIED, DWMBlocks.GALLIFREY_COAL_ORE, 17, 0.5F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_IRON_ORE, DWMBlocks.GALLIFREY_IRON_ORE, 9, 0.0F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_IRON_ORE_SMALL, DWMBlocks.GALLIFREY_IRON_ORE, 4, 0.0F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_GOLD_ORE, DWMBlocks.GALLIFREY_GOLD_ORE, 9, 0.0F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_GOLD_ORE_BURIED, DWMBlocks.GALLIFREY_GOLD_ORE, 9, 0.5F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_SMALL, DWMBlocks.GALLIFREY_DIAMOND_ORE, 4, 0.5F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_MEDIUM, DWMBlocks.GALLIFREY_DIAMOND_ORE, 8, 0.5F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_LARGE, DWMBlocks.GALLIFREY_DIAMOND_ORE, 12, 0.7F);
        registerOre(registerable, DWMConfiguredFeatures.GALLIFREY_DIAMOND_ORE_BURIED, DWMBlocks.GALLIFREY_DIAMOND_ORE, 8, 1.0F);

        registerPetrifiedTree(
                registerable,
                DWMConfiguredFeatures.PETRIFIED_TREE,
                new FancyTrunkPlacer(3, 11, 0),
                new BlobFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), 2),
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
        );
        registerPetrifiedTree(
                registerable,
                DWMConfiguredFeatures.PETRIFIED_SNAG,
                new StraightTrunkPlacer(4, 2, 0),
                new BlobFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), 1),
                new TwoLayersFeatureSize(1, 0, 1)
        );
        registerable.register(
                DWMConfiguredFeatures.FALLEN_PETRIFIED_TREE,
                FallenTreeFeature.builder(
                        BlockStateProvider.of(DWMBlocks.PETRIFIED_LOG),
                        UniformInt.of(4, 11)
                ).build()
        );
    }

    private static void registerOre(
            BootstrapContext<Feature> registerable,
            ResourceKey<Feature> key,
            Block oreBlock,
            int size,
            float discardChanceOnAirExposure
    ) {
        registerOre(registerable, key, oreBlock, DWMBlockTags.GALLIFREY_ORE_REPLACEABLES, size, discardChanceOnAirExposure);
    }

    private static void registerOre(
            BootstrapContext<Feature> registerable,
            ResourceKey<Feature> key,
            Block oreBlock,
            TagKey<Block> replaceable,
            int size,
            float discardChanceOnAirExposure
    ) {
        registerable.register(
                key,
                new OreFeature(
                        java.util.List.of(BlockReplacement.replace(new TagMatchTest(replaceable), oreBlock.defaultBlockState())),
                        size,
                        discardChanceOnAirExposure
                )
        );
    }

    private static void registerTree(
            BootstrapContext<Feature> registerable,
            ResourceKey<Feature> key,
            Block logBlock,
            Block leavesBlock
    ) {
        registerable.register(
                key,
                new TreeFeature.Builder(
                        BlockStateProvider.of(logBlock.defaultBlockState()),
                        new StraightTrunkPlacer(4, 2, 0),
                        BlockStateProvider.of(leavesBlock.defaultBlockState()),
                        new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                        new TwoLayersFeatureSize(1, 0, 1),
                        registerable.lookup(Registries.BLOCK_STATE_PROVIDER).getOrThrow(BlockStateProviders.SOIL_BENEATH_TREE)
                ).ignoreVines().build()
        );
    }

    /**
     * Dead mineralized trunks: trunk and "foliage" are both petrified log (no leaves/saplings).
     */
    private static void registerPetrifiedTree(
            BootstrapContext<Feature> registerable,
            ResourceKey<Feature> key,
            TrunkPlacer trunkPlacer,
            FoliagePlacer foliagePlacer,
            TwoLayersFeatureSize minimumSize
    ) {
        BlockState log = DWMBlocks.PETRIFIED_LOG.defaultBlockState();
        registerable.register(
                key,
                new TreeFeature.Builder(
                        BlockStateProvider.of(log),
                        trunkPlacer,
                        BlockStateProvider.of(log),
                        foliagePlacer,
                        minimumSize,
                        registerable.lookup(Registries.BLOCK_STATE_PROVIDER).getOrThrow(BlockStateProviders.SOIL_BENEATH_TREE)
                ).ignoreVines().build()
        );
    }
}
