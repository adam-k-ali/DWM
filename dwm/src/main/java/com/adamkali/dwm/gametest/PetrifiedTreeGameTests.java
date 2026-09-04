package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.world.DWMConfiguredFeatures;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PetrifiedTreeGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void petrifiedSnagPlacesLogWithoutLeavesOrSaplings(GameTestHelper context) {
        BlockPos dirtRel = new BlockPos(3, 0, 3);
        BlockPos trunkOriginRel = dirtRel.above();
        context.setBlock(dirtRel, Blocks.PODZOL.defaultBlockState());
        for (int y = 1; y < 8; y++) {
            context.setBlock(dirtRel.above(y), Blocks.AIR.defaultBlockState());
        }

        ServerLevel world = context.getLevel();
        BlockPos trunkOriginAbs = context.absolutePos(trunkOriginRel);
        ConfiguredFeature<?, ?> snag = world.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .get(DWMConfiguredFeatures.PETRIFIED_SNAG)
                .orElseThrow(() -> new AssertionError("Expected configured feature dwm:petrified_snag"))
                .value();

        boolean placed = false;
        for (long seed : new long[] {1L, 2L, 3L, 7L, 13L, 42L, 99L}) {
            clearColumn(context, dirtRel);
            context.setBlock(dirtRel, Blocks.PODZOL.defaultBlockState());
            if (snag.place(world, world.getChunkSource().getGenerator(), RandomSource.create(seed), trunkOriginAbs)
                    && containsBlockInBox(context, DWMBlocks.PETRIFIED_LOG)) {
                placed = true;
                break;
            }
        }

        if (!placed) {
            throw new AssertionError("Expected petrified snag to place petrified_log on podzol");
        }
        if (containsLivingWoodInBox(context)) {
            throw new AssertionError("Petrified snag must not place leaves or saplings");
        }

        context.succeed();
    }

    private static void clearColumn(GameTestHelper context, BlockPos dirtRel) {
        for (int y = 0; y < 8; y++) {
            context.setBlock(dirtRel.above(y), Blocks.AIR.defaultBlockState());
        }
    }

    private static boolean containsBlockInBox(GameTestHelper context, Block block) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    if (context.getLevel().getBlockState(context.absolutePos(new BlockPos(x, y, z))).is(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean containsLivingWoodInBox(GameTestHelper context) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    Block block = context.getLevel().getBlockState(context.absolutePos(new BlockPos(x, y, z))).getBlock();
                    if (block instanceof LeavesBlock || block instanceof SaplingBlock || block.defaultBlockState().is(BlockTags.LEAVES)) {
                        return true;
                    }
                    String path = block.builtInRegistryHolder().key().identifier().getPath();
                    if (path.contains("leaves") || path.contains("sapling")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
