package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.item.DWMItems;
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

public class DalekaniumOreGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dalekaniumOreDropsRawWithStonePickaxe(GameTestHelper context) {
        assertOreDrops(context, DWMBlocks.DALEKANIUM_ORE, Items.STONE_PICKAXE, DWMItems.RAW_DALEKANIUM, "dalekanium ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dalekaniumOreRejectsWoodenPickaxeAsCorrectTool(GameTestHelper context) {
        BlockState state = DWMBlocks.DALEKANIUM_ORE.defaultBlockState();
        ItemStack pickaxe = new ItemStack(Items.WOODEN_PICKAXE);
        if (pickaxe.isCorrectToolForDrops(state)) {
            throw new AssertionError("Expected wooden pickaxe to be incorrect for dalekanium ore");
        }
        if (!state.requiresCorrectToolForDrops()) {
            throw new AssertionError("Expected dalekanium ore to require the correct tool for drops");
        }
        context.succeed();
    }

    private static void assertOreDrops(
            GameTestHelper context,
            Block ore,
            Item pickaxeItem,
            Item expectedDrop,
            String label
    ) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockPos orePos = new BlockPos(1, 1, 1);
        context.setBlock(orePos, ore.defaultBlockState());

        BlockState state = ore.defaultBlockState();
        ItemStack pickaxe = new ItemStack(pickaxeItem);
        if (!pickaxe.isCorrectToolForDrops(state)) {
            throw new AssertionError("Expected " + pickaxeItem + " to be correct tool for " + label);
        }

        List<ItemStack> drops = getDrops(context, player, orePos, pickaxe);
        int actual = 0;
        for (ItemStack stack : drops) {
            if (stack.is(expectedDrop)) {
                actual += stack.getCount();
            }
        }
        if (actual != 1) {
            throw new AssertionError(
                    "Expected 1x " + expectedDrop + " from " + label + " but got " + actual + " in " + drops
            );
        }
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
}
