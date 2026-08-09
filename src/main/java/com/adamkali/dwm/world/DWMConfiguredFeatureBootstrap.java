package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

public final class DWMConfiguredFeatureBootstrap {
    private DWMConfiguredFeatureBootstrap() {
    }

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> registerable) {
        registerTree(registerable, DWMConfiguredFeatures.ASH, DWMBlocks.ASH_LOG, DWMBlocks.ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.DARK_ASH, DWMBlocks.DARK_ASH_LOG, DWMBlocks.DARK_ASH_LEAVES);
        registerTree(registerable, DWMConfiguredFeatures.CARDINAL, DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LEAVES);
    }

    private static void registerTree(
            Registerable<ConfiguredFeature<?, ?>> registerable,
            RegistryKey<ConfiguredFeature<?, ?>> key,
            Block logBlock,
            Block leavesBlock
    ) {
        BlockState log = logBlock.getDefaultState();
        BlockState leaves = leavesBlock.getDefaultState();
        registerable.register(
                key,
                new ConfiguredFeature<>(
                        Feature.TREE,
                        new TreeFeatureConfig.Builder(
                                BlockStateProvider.of(log),
                                new StraightTrunkPlacer(4, 2, 0),
                                BlockStateProvider.of(leaves),
                                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3),
                                new TwoLayersFeatureSize(1, 0, 1)
                        ).ignoreVines().build()
                )
        );
    }
}
