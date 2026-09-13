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
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class DalekaniumGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void oreDropsSelfWithIronPickaxe(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockPos orePos = new BlockPos(1, 1, 1);
        context.setBlock(orePos, DWMBlocks.DALEKANIUM_ORE.defaultBlockState());

        BlockState state = DWMBlocks.DALEKANIUM_ORE.defaultBlockState();
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
        if (!ironPickaxe.isCorrectToolForDrops(state)) {
            throw new AssertionError("Expected iron pickaxe to be correct tool for dalekanium ore");
        }

        List<ItemStack> drops = getDrops(context, player, orePos, ironPickaxe);
        assertHasItem(drops, DWMBlocks.DALEKANIUM_ORE.asItem(), 1, "dalekanium ore with iron pickaxe");

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void oreRejectsStonePickaxeAsCorrectTool(GameTestHelper context) {
        BlockState state = DWMBlocks.DALEKANIUM_ORE.defaultBlockState();
        ItemStack stonePickaxe = new ItemStack(Items.STONE_PICKAXE);
        if (stonePickaxe.isCorrectToolForDrops(state)) {
            throw new AssertionError("Expected stone pickaxe to be incorrect for dalekanium ore drops");
        }
        if (!state.requiresCorrectToolForDrops()) {
            throw new AssertionError("Expected dalekanium ore to require the correct tool for drops");
        }
        context.succeed();
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
