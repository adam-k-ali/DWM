package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void oreSmeltsAndBlastsToSilverIngot(GameTestHelper context) {
        SingleRecipeInput input = new SingleRecipeInput(new ItemStack(DWMBlocks.DALEKANIUM_ORE));
        assertCooks(
                context,
                "silver_dalekanium_ingot_from_smelting_dalekanium_ore",
                RecipeType.SMELTING,
                input,
                DWMItems.SILVER_DALEKANIUM_INGOT,
                1
        );
        assertCooks(
                context,
                "silver_dalekanium_ingot_from_blasting_dalekanium_ore",
                RecipeType.BLASTING,
                input,
                DWMItems.SILVER_DALEKANIUM_INGOT,
                1
        );
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void craftingRecipesProduceExpectedOutputs(GameTestHelper context) {
        assertCrafts(
                context,
                "bronze_dalekanium_ingot",
                grid(2, 1, DWMItems.SILVER_DALEKANIUM_INGOT, Items.COPPER_INGOT),
                DWMItems.BRONZE_DALEKANIUM_INGOT,
                1
        );

        assertDalekaniumToolRecipes(
                context,
                "silver_dalekanium",
                DWMItems.SILVER_DALEKANIUM_INGOT,
                DWMItems.SILVER_DALEKANIUM_SWORD,
                DWMItems.SILVER_DALEKANIUM_SHOVEL,
                DWMItems.SILVER_DALEKANIUM_PICKAXE,
                DWMItems.SILVER_DALEKANIUM_AXE,
                DWMItems.SILVER_DALEKANIUM_HOE
        );
        assertDalekaniumToolRecipes(
                context,
                "bronze_dalekanium",
                DWMItems.BRONZE_DALEKANIUM_INGOT,
                DWMItems.BRONZE_DALEKANIUM_SWORD,
                DWMItems.BRONZE_DALEKANIUM_SHOVEL,
                DWMItems.BRONZE_DALEKANIUM_PICKAXE,
                DWMItems.BRONZE_DALEKANIUM_AXE,
                DWMItems.BRONZE_DALEKANIUM_HOE
        );

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void silverAndBronzePickaxesMineDalekaniumOre(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockState dalekaniumOre = DWMBlocks.DALEKANIUM_ORE.defaultBlockState();
        BlockState goldOre = Blocks.GOLD_ORE.defaultBlockState();
        ItemStack silverPickaxe = new ItemStack(DWMItems.SILVER_DALEKANIUM_PICKAXE);
        ItemStack bronzePickaxe = new ItemStack(DWMItems.BRONZE_DALEKANIUM_PICKAXE);

        if (!silverPickaxe.isCorrectToolForDrops(dalekaniumOre)) {
            throw new AssertionError("Expected silver dalekanium pickaxe to be correct tool for dalekanium ore");
        }
        if (!bronzePickaxe.isCorrectToolForDrops(dalekaniumOre)) {
            throw new AssertionError("Expected bronze dalekanium pickaxe to be correct tool for dalekanium ore");
        }
        if (!silverPickaxe.isCorrectToolForDrops(goldOre)) {
            throw new AssertionError("Expected silver dalekanium pickaxe to be correct tool for gold ore");
        }

        BlockPos orePos = new BlockPos(1, 1, 1);
        context.setBlock(orePos, dalekaniumOre);
        assertHasItem(
                getDrops(context, player, orePos, silverPickaxe),
                DWMBlocks.DALEKANIUM_ORE.asItem(),
                1,
                "dalekanium ore with silver dalekanium pickaxe"
        );

        context.setBlock(orePos, dalekaniumOre);
        assertHasItem(
                getDrops(context, player, orePos, bronzePickaxe),
                DWMBlocks.DALEKANIUM_ORE.asItem(),
                1,
                "dalekanium ore with bronze dalekanium pickaxe"
        );

        context.succeed();
    }

    private static void assertDalekaniumToolRecipes(
            GameTestHelper context,
            String prefix,
            Item ingot,
            Item sword,
            Item shovel,
            Item pickaxe,
            Item axe,
            Item hoe
    ) {
        assertCrafts(context, prefix + "_sword", grid(1, 3, ingot, ingot, Items.STICK), sword, 1);
        assertCrafts(context, prefix + "_shovel", grid(1, 3, ingot, Items.STICK, Items.STICK), shovel, 1);
        assertCrafts(
                context,
                prefix + "_pickaxe",
                grid(3, 3,
                        ingot, ingot, ingot,
                        Items.AIR, Items.STICK, Items.AIR,
                        Items.AIR, Items.STICK, Items.AIR),
                pickaxe,
                1
        );
        assertCrafts(
                context,
                prefix + "_axe",
                grid(2, 3,
                        ingot, ingot,
                        ingot, Items.STICK,
                        Items.AIR, Items.STICK),
                axe,
                1
        );
        assertCrafts(
                context,
                prefix + "_hoe",
                grid(2, 3,
                        ingot, ingot,
                        Items.AIR, Items.STICK,
                        Items.AIR, Items.STICK),
                hoe,
                1
        );
    }

    private static <T extends AbstractCookingRecipe> void assertCooks(
            GameTestHelper context,
            String recipePath,
            RecipeType<T> type,
            SingleRecipeInput input,
            Item expected,
            int count
    ) {
        ServerLevel world = context.getLevel();
        RecipeManager recipes = world.getServer().getRecipeManager();
        ResourceKey<Recipe<?>> key = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, recipePath)
        );
        Optional<RecipeHolder<?>> byId = recipes.byKey(key);
        if (byId.isEmpty()) {
            throw new AssertionError("Missing recipe dwm:" + recipePath);
        }
        if (!(byId.get().value() instanceof AbstractCookingRecipe cookingRecipe)) {
            throw new AssertionError("Recipe dwm:" + recipePath + " is not a cooking recipe");
        }
        if (!cookingRecipe.matches(input, world)) {
            throw new AssertionError("Recipe dwm:" + recipePath + " did not match cooking input");
        }

        Optional<RecipeHolder<T>> match = recipes.getRecipeFor(type, input, world);
        if (match.isEmpty()) {
            throw new AssertionError("No cooking match for dwm:" + recipePath);
        }

        ItemStack result = cookingRecipe.assemble(input);
        if (!result.is(expected) || result.getCount() != count) {
            throw new AssertionError(
                    "Recipe dwm:" + recipePath + " expected " + count + "x " + expected
                            + " but got " + result.getCount() + "x " + result.getItem()
            );
        }
    }

    private static void assertCrafts(
            GameTestHelper context,
            String recipePath,
            CraftingInput input,
            Item expected,
            int count
    ) {
        ServerLevel world = context.getLevel();
        RecipeManager recipes = world.getServer().getRecipeManager();
        ResourceKey<Recipe<?>> key = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, recipePath)
        );
        Optional<RecipeHolder<?>> byId = recipes.byKey(key);
        if (byId.isEmpty()) {
            throw new AssertionError("Missing recipe dwm:" + recipePath);
        }
        if (!(byId.get().value() instanceof CraftingRecipe craftingRecipe)) {
            throw new AssertionError("Recipe dwm:" + recipePath + " is not a crafting recipe");
        }
        if (!craftingRecipe.matches(input, world)) {
            throw new AssertionError("Recipe dwm:" + recipePath + " did not match crafted input");
        }

        Optional<RecipeHolder<CraftingRecipe>> match = recipes.getRecipeFor(RecipeType.CRAFTING, input, world);
        if (match.isEmpty()) {
            throw new AssertionError("No crafting match for dwm:" + recipePath);
        }

        ItemStack result = craftingRecipe.assemble(input);
        if (!result.is(expected) || result.getCount() != count) {
            throw new AssertionError(
                    "Recipe dwm:" + recipePath + " expected " + count + "x " + expected
                            + " but got " + result.getCount() + "x " + result.getItem()
            );
        }
    }

    private static CraftingInput grid(int width, int height, Object... cells) {
        if (cells.length != width * height) {
            throw new IllegalArgumentException("Grid size mismatch");
        }
        List<ItemStack> stacks = new ArrayList<>(cells.length);
        for (Object cell : cells) {
            stacks.add(stackOf(cell));
        }
        return CraftingInput.of(width, height, stacks);
    }

    private static ItemStack stackOf(Object cell) {
        if (cell == Items.AIR || cell == null) {
            return ItemStack.EMPTY;
        }
        if (cell instanceof Item item) {
            return new ItemStack(item);
        }
        if (cell instanceof Block block) {
            return new ItemStack(block);
        }
        if (cell instanceof ItemStack stack) {
            return stack;
        }
        throw new IllegalArgumentException("Unsupported grid cell: " + cell);
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
