package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registerable;
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
        BlockState log = DWMBlocks.ASH_LOG.getDefaultState();
        BlockState leaves = DWMBlocks.ASH_LEAVES.getDefaultState();
        registerable.register(
                DWMConfiguredFeatures.ASH,
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
