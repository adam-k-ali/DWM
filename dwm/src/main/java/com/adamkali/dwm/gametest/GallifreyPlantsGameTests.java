package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class GallifreyPlantsGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void flowerSurvivesOnGallifreyGrassAndPopsOffStone(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(1, 1, 1);
        BlockPos flowerRel = grassRel.above();

        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());
        context.setBlock(flowerRel, DWMBlocks.FLOWER_OF_REMEMBRANCE.defaultBlockState());
        context.assertBlockPresent(DWMBlocks.FLOWER_OF_REMEMBRANCE, flowerRel);

        BlockPos flowerAbs = context.absolutePos(flowerRel);
        ServerLevel world = context.getLevel();
        BlockState flowerState = world.getBlockState(flowerAbs);
        if (!flowerState.canSurvive(world, flowerAbs)) {
            throw new AssertionError("Expected flower of remembrance to survive on Gallifrey grass");
        }

        context.setBlock(grassRel, Blocks.STONE.defaultBlockState());
        // Neighbor change should collapse vegetation immediately via updateShape.
        context.assertBlockPresent(Blocks.AIR, flowerRel);

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void pottedFlowerDropsPotAndFlower(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pottedPos = new BlockPos(2, 1, 1);
        context.setBlock(pottedPos, DWMBlocks.POTTED_FLOWER_OF_REMEMBRANCE.defaultBlockState());

        List<ItemStack> drops = getDrops(context, player, pottedPos, ItemStack.EMPTY);
        assertHasItem(drops, Items.FLOWER_POT, 1, "potted flower of remembrance");
        assertHasItem(drops, DWMBlocks.FLOWER_OF_REMEMBRANCE.asItem(), 1, "potted flower of remembrance");

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void saccharineCaneStacksOnSandAndBreaksWithoutSupport(GameTestHelper context) {
        BlockPos sandRel = new BlockPos(3, 1, 1);
        BlockPos baseCaneRel = sandRel.above();
        BlockPos topCaneRel = baseCaneRel.above();

        context.setBlock(sandRel, DWMBlocks.GALLIFREY_SAND.defaultBlockState());
        context.setBlock(baseCaneRel, DWMBlocks.SACCHARINE_CANE.defaultBlockState());
        context.setBlock(topCaneRel, DWMBlocks.SACCHARINE_CANE.defaultBlockState());
        context.assertBlockPresent(DWMBlocks.SACCHARINE_CANE, baseCaneRel);
        context.assertBlockPresent(DWMBlocks.SACCHARINE_CANE, topCaneRel);

        BlockPos topAbs = context.absolutePos(topCaneRel);
        if (!context.getLevel().getBlockState(topAbs).canSurvive(context.getLevel(), topAbs)) {
            throw new AssertionError("Expected stacked saccharine cane to survive on cane below");
        }

        // Remove base support; cane schedules a break tick.
        context.setBlock(sandRel, Blocks.AIR.defaultBlockState());
        context.runAtTickTime(context.getTick() + 2, () -> {
            context.assertBlockPresent(Blocks.AIR, baseCaneRel);
            context.assertBlockPresent(Blocks.AIR, topCaneRel);
            context.succeed();
        });
    }

    private static List<ItemStack> getDrops(
            GameTestHelper context,
            Player player,
            BlockPos relativePos,
            ItemStack tool
    ) {
        BlockPos abs = context.absolutePos(relativePos);
        ServerLevel world = context.getLevel();
        BlockState state = world.getBlockState(abs);
        return Block.getDrops(state, world, abs, world.getBlockEntity(abs), player, tool);
    }

    private static void assertHasItem(List<ItemStack> drops, Item expected, int count, String label) {
        int actual = 0;
        for (ItemStack stack : drops) {
            if (stack.is(expected)) {
                actual += stack.getCount();
            }
        }
        if (actual != count) {
            throw new AssertionError(
                    "Expected " + count + "x " + expected + " from " + label + " but got " + actual + " in " + drops
            );
        }
    }
}
