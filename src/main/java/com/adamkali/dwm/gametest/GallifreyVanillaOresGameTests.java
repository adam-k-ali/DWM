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

public class GallifreyVanillaOresGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void coalOreDropsCoalWithWoodenPickaxe(GameTestHelper context) {
        assertOreDrops(context, DWMBlocks.GALLIFREY_COAL_ORE, Items.WOODEN_PICKAXE, Items.COAL, "gallifrey coal ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void ironOreDropsRawIronWithStonePickaxe(GameTestHelper context) {
        assertOreDrops(context, DWMBlocks.GALLIFREY_IRON_ORE, Items.STONE_PICKAXE, Items.RAW_IRON, "gallifrey iron ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void ironOreRejectsWoodenPickaxeAsCorrectTool(GameTestHelper context) {
        assertRejectsTool(DWMBlocks.GALLIFREY_IRON_ORE, Items.WOODEN_PICKAXE, "gallifrey iron ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void goldOreDropsRawGoldWithIronPickaxe(GameTestHelper context) {
        assertOreDrops(context, DWMBlocks.GALLIFREY_GOLD_ORE, Items.IRON_PICKAXE, Items.RAW_GOLD, "gallifrey gold ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void goldOreRejectsStonePickaxeAsCorrectTool(GameTestHelper context) {
        assertRejectsTool(DWMBlocks.GALLIFREY_GOLD_ORE, Items.STONE_PICKAXE, "gallifrey gold ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void diamondOreDropsDiamondWithIronPickaxe(GameTestHelper context) {
        assertOreDrops(context, DWMBlocks.GALLIFREY_DIAMOND_ORE, Items.IRON_PICKAXE, Items.DIAMOND, "gallifrey diamond ore");
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void diamondOreRejectsStonePickaxeAsCorrectTool(GameTestHelper context) {
        assertRejectsTool(DWMBlocks.GALLIFREY_DIAMOND_ORE, Items.STONE_PICKAXE, "gallifrey diamond ore");
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
        assertHasItem(drops, expectedDrop, 1, label);
    }

    private static void assertRejectsTool(Block ore, Item pickaxeItem, String label) {
        BlockState state = ore.defaultBlockState();
        ItemStack pickaxe = new ItemStack(pickaxeItem);
        if (pickaxe.isCorrectToolForDrops(state)) {
            throw new AssertionError("Expected " + pickaxeItem + " to be incorrect for " + label);
        }
        if (!state.requiresCorrectToolForDrops()) {
            throw new AssertionError("Expected " + label + " to require the correct tool for drops");
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
