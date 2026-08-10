package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

public final class DWMConfiguredFeatureBootstrap {
    private DWMConfiguredFeatureBootstrap() {
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> registerable) {
        registerTree(registerable, DWMConfiguredFeatures.ASH, DWMBlocks.ASH_LOG, DWMBlocks.ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.DARK_ASH, DWMBlocks.DARK_ASH_LOG, DWMBlocks.DARK_ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.CARDINAL, DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LEAVES);
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
