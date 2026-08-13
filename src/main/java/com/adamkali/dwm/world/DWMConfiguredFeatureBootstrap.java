package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlockTags;
import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public final class DWMConfiguredFeatureBootstrap {
    private DWMConfiguredFeatureBootstrap() {
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> registerable) {
        registerTree(registerable, DWMConfiguredFeatures.ASH, DWMBlocks.ASH_LOG, DWMBlocks.ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.DARK_ASH, DWMBlocks.DARK_ASH_LOG, DWMBlocks.DARK_ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.CARDINAL, DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LEAVES);

        WeightedList.Builder<BlockState> flowers = WeightedList.builder();
        flowers.add(DWMBlocks.FLOWER_OF_REMEMBRANCE.defaultBlockState(), 2);
        flowers.add(DWMBlocks.MOONLIGHT_BLOOM.defaultBlockState(), 1);
        registerable.register(
                DWMConfiguredFeatures.GALLIFREY_FLOWERS,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(new WeightedStateProvider(flowers))
                )
        );

        registerable.register(
                DWMConfiguredFeatures.SACCHARINE_CANE,
                new ConfiguredFeature<>(
                        Feature.BLOCK_COLUMN,
                        BlockColumnConfiguration.simple(
                                BiasedToBottomInt.of(2, 4),
                                BlockStateProvider.simple(DWMBlocks.SACCHARINE_CANE)
                        )
                )
        );

        registerable.register(
                DWMConfiguredFeatures.AZBANTIUM_ORE,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new TagMatchTest(DWMBlockTags.GALLIFREY_ORE_REPLACEABLES),
                                DWMBlocks.AZBANTIUM_ORE.defaultBlockState(),
                                9,
                                0.5F
                        )
                )
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
    }

    private static void registerOre(
            BootstrapContext<ConfiguredFeature<?, ?>> registerable,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            Block oreBlock,
            int size,
            float discardChanceOnAirExposure
    ) {
        registerable.register(
                key,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new TagMatchTest(DWMBlockTags.GALLIFREY_ORE_REPLACEABLES),
                                oreBlock.defaultBlockState(),
                                size,
                                discardChanceOnAirExposure
                        )
                )
        );
    }

    private static void registerTree(
            BootstrapContext<ConfiguredFeature<?, ?>> registerable,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            Block logBlock,
            Block leavesBlock
    ) {
        BlockState log = logBlock.defaultBlockState();
        BlockState leaves = leavesBlock.defaultBlockState();
        registerable.register(
                key,
                new ConfiguredFeature<>(
                        Feature.TREE,
                        new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(log),
                                new StraightTrunkPlacer(4, 2, 0),
                                BlockStateProvider.simple(leaves),
                                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                                new TwoLayersFeatureSize(1, 0, 1),
                                TreeConfiguration.defaultPlaceBelowTreeTrunkProvider(
                                        registerable.lookup(Registries.BIOME)
                                )
                        ).ignoreVines().build()
                )
        );
    }
}
