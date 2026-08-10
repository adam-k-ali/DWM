package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AshWoodFamilyGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void craftingRecipesProduceExpectedOutputs(GameTestHelper context) {
        assertCrafts(context, "ash_planks", grid(1, 1, DWMBlocks.ASH_LOG), DWMBlocks.ASH_PLANKS.asItem(), 4);
        assertCrafts(context, "ash_planks", grid(1, 1, DWMBlocks.STRIPPED_ASH_WOOD), DWMBlocks.ASH_PLANKS.asItem(), 4);

        assertCrafts(context, "ash_wood", grid(2, 2,
                DWMBlocks.ASH_LOG, DWMBlocks.ASH_LOG,
                DWMBlocks.ASH_LOG, DWMBlocks.ASH_LOG), DWMBlocks.ASH_WOOD.asItem(), 3);
        assertCrafts(context, "stripped_ash_wood", grid(2, 2,
                DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG,
                DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG), DWMBlocks.STRIPPED_ASH_WOOD.asItem(), 3);

        assertCrafts(context, "ash_stairs", grid(3, 3,
                DWMBlocks.ASH_PLANKS, Items.AIR, Items.AIR,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, Items.AIR,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS), DWMBlocks.ASH_STAIRS.asItem(), 4);
        assertCrafts(context, "ash_slab", grid(3, 1,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS), DWMBlocks.ASH_SLAB.asItem(), 6);
        assertCrafts(context, "ash_fence", grid(3, 2,
                DWMBlocks.ASH_PLANKS, Items.STICK, DWMBlocks.ASH_PLANKS,
                DWMBlocks.ASH_PLANKS, Items.STICK, DWMBlocks.ASH_PLANKS), DWMBlocks.ASH_FENCE.asItem(), 3);
        assertCrafts(context, "ash_fence_gate", grid(3, 2,
                Items.STICK, DWMBlocks.ASH_PLANKS, Items.STICK,
                Items.STICK, DWMBlocks.ASH_PLANKS, Items.STICK), DWMBlocks.ASH_FENCE_GATE.asItem(), 1);
        assertCrafts(context, "ash_pressure_plate", grid(2, 1,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS), DWMBlocks.ASH_PRESSURE_PLATE.asItem(), 1);
        assertCrafts(context, "ash_button", grid(1, 1, DWMBlocks.ASH_PLANKS), DWMBlocks.ASH_BUTTON.asItem(), 1);
        assertCrafts(context, "ash_sign", grid(3, 3,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS,
                Items.AIR, Items.STICK, Items.AIR), DWMItems.ASH_SIGN, 3);
        assertCrafts(context, "ash_boat", grid(3, 2,
                DWMBlocks.ASH_PLANKS, Items.AIR, DWMBlocks.ASH_PLANKS,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS), DWMItems.ASH_BOAT, 1);
        assertCrafts(context, "ash_hanging_sign", grid(3, 3,
                Items.IRON_CHAIN, Items.AIR, Items.IRON_CHAIN,
                DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG,
                DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG),
                DWMItems.ASH_HANGING_SIGN, 6);
        assertCrafts(context, "ash_door", grid(3, 3,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, Items.AIR,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, Items.AIR,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, Items.AIR), DWMBlocks.ASH_DOOR.asItem(), 3);
        assertCrafts(context, "ash_trapdoor", grid(3, 2,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS,
                DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS),
                DWMBlocks.ASH_TRAPDOOR.asItem(), 2);

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void blockLootDropsExpectedItems(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);

        assertSelfDrop(context, player, DWMBlocks.ASH_PLANKS, DWMBlocks.ASH_PLANKS.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_LOG, DWMBlocks.ASH_LOG.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_WOOD, DWMBlocks.ASH_WOOD.asItem());
        assertSelfDrop(context, player, DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_LOG.asItem());
        assertSelfDrop(context, player, DWMBlocks.STRIPPED_ASH_WOOD, DWMBlocks.STRIPPED_ASH_WOOD.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_STAIRS, DWMBlocks.ASH_STAIRS.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_FENCE, DWMBlocks.ASH_FENCE.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_FENCE_GATE, DWMBlocks.ASH_FENCE_GATE.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_BUTTON, DWMBlocks.ASH_BUTTON.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_PRESSURE_PLATE, DWMBlocks.ASH_PRESSURE_PLATE.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_DOOR, DWMBlocks.ASH_DOOR.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_TRAPDOOR, DWMBlocks.ASH_TRAPDOOR.asItem());
        assertSelfDrop(context, player, DWMBlocks.ASH_SIGN, DWMItems.ASH_SIGN);
        assertSelfDrop(context, player, DWMBlocks.ASH_HANGING_SIGN, DWMItems.ASH_HANGING_SIGN);
        assertSelfDrop(context, player, DWMBlocks.ASH_SAPLING, DWMBlocks.ASH_SAPLING.asItem());

        BlockPos slabPos = new BlockPos(1, 1, 1);
        context.setBlock(slabPos, DWMBlocks.ASH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        assertDropsContain(context, player, slabPos, ItemStack.EMPTY, DWMBlocks.ASH_SLAB.asItem(), 1);

        context.setBlock(slabPos, DWMBlocks.ASH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
        assertDropsContain(context, player, slabPos, ItemStack.EMPTY, DWMBlocks.ASH_SLAB.asItem(), 2);

        BlockPos wallSignPos = new BlockPos(2, 1, 1);
        context.setBlock(wallSignPos, DWMBlocks.ASH_WALL_SIGN.defaultBlockState());
        assertDropsContain(context, player, wallSignPos, ItemStack.EMPTY, DWMItems.ASH_SIGN, 1);

        BlockPos wallHangingPos = new BlockPos(3, 1, 1);
        context.setBlock(wallHangingPos, DWMBlocks.ASH_WALL_HANGING_SIGN.defaultBlockState());
        assertDropsContain(context, player, wallHangingPos, ItemStack.EMPTY, DWMItems.ASH_HANGING_SIGN, 1);

        BlockPos pottedPos = new BlockPos(4, 1, 1);
        context.setBlock(pottedPos, DWMBlocks.POTTED_ASH_SAPLING.defaultBlockState());
        List<ItemStack> pottedDrops = getDrops(context, player, pottedPos, ItemStack.EMPTY);
        assertHasItem(pottedDrops, Items.FLOWER_POT, 1, "potted ash sapling");
        assertHasItem(pottedDrops, DWMBlocks.ASH_SAPLING.asItem(), 1, "potted ash sapling");

        BlockPos leavesPos = new BlockPos(5, 1, 1);
        context.setBlock(leavesPos, DWMBlocks.ASH_LEAVES.defaultBlockState());
        List<ItemStack> shearsDrops = getDrops(context, player, leavesPos, new ItemStack(Items.SHEARS));
        assertHasItem(shearsDrops, DWMBlocks.ASH_LEAVES.asItem(), 1, "ash leaves with shears");

        List<ItemStack> bareDrops = getDrops(context, player, leavesPos, ItemStack.EMPTY);
        if (countItem(bareDrops, DWMBlocks.ASH_LEAVES.asItem()) > 0) {
            throw new AssertionError("Expected ash leaves without shears/silk touch not to drop leaves");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void orientedBlocksAcceptSupportedDirections(GameTestHelper context) {
        for (Block log : List.of(
                DWMBlocks.ASH_LOG, DWMBlocks.ASH_WOOD, DWMBlocks.STRIPPED_ASH_LOG, DWMBlocks.STRIPPED_ASH_WOOD)) {
            for (Direction.Axis axis : Direction.Axis.values()) {
                BlockPos pos = new BlockPos(1 + axis.ordinal(), 1, 1);
                context.setBlock(pos, log.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
                context.assertBlockProperty(pos, RotatedPillarBlock.AXIS, axis);
            }
        }

        int stairX = 1;
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockPos pos = new BlockPos(stairX++, 2, 2);
            context.setBlock(pos, DWMBlocks.ASH_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing));
            context.assertBlockProperty(pos, StairBlock.FACING, facing);
        }

        BlockPos bottomSlab = new BlockPos(1, 3, 1);
        context.setBlock(bottomSlab, DWMBlocks.ASH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        context.assertBlockProperty(bottomSlab, SlabBlock.TYPE, SlabType.BOTTOM);
        BlockPos topSlab = new BlockPos(2, 3, 1);
        context.setBlock(topSlab, DWMBlocks.ASH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        context.assertBlockProperty(topSlab, SlabBlock.TYPE, SlabType.TOP);
        BlockPos doubleSlab = new BlockPos(3, 3, 1);
        context.setBlock(doubleSlab, DWMBlocks.ASH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
        context.assertBlockProperty(doubleSlab, SlabBlock.TYPE, SlabType.DOUBLE);

        context.setBlock(1, 3, 4, Blocks.STONE);
        BlockPos floorButton = new BlockPos(1, 4, 4);
        context.setBlock(floorButton, DWMBlocks.ASH_BUTTON.defaultBlockState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR)
                .setValue(ButtonBlock.FACING, Direction.NORTH));
        context.assertBlockProperty(floorButton, FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR);

        context.setBlock(2, 4, 4, Blocks.STONE);
        BlockPos wallButton = new BlockPos(3, 4, 4);
        context.setBlock(wallButton, DWMBlocks.ASH_BUTTON.defaultBlockState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL)
                .setValue(ButtonBlock.FACING, Direction.EAST));
        context.assertBlockProperty(wallButton, FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL);

        context.setBlock(5, 5, 4, Blocks.STONE);
        BlockPos ceilingButton = new BlockPos(5, 4, 4);
        context.setBlock(ceilingButton, DWMBlocks.ASH_BUTTON.defaultBlockState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.CEILING)
                .setValue(ButtonBlock.FACING, Direction.SOUTH));
        context.assertBlockProperty(ceilingButton, FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.CEILING);

        int gateX = 1;
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockPos pos = new BlockPos(gateX++, 5, 3);
            context.setBlock(pos, DWMBlocks.ASH_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, facing));
            context.assertBlockProperty(pos, FenceGateBlock.FACING, facing);
        }

        context.setBlock(5, 5, 5, Blocks.STONE);
        context.setBlock(5, 6, 5, DWMBlocks.ASH_PRESSURE_PLATE.defaultBlockState());
        context.assertBlockPresent(DWMBlocks.ASH_PRESSURE_PLATE, 5, 6, 5);

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void boatPlacesOnWater(GameTestHelper context) {
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                context.setBlock(x, 0, z, Blocks.STONE);
                context.setBlock(x, 1, z, Blocks.WATER);
            }
        }

        BlockPos playerRel = new BlockPos(3, 3, 3);
        BlockPos playerAbs = context.absolutePos(playerRel);
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(playerAbs.getX() + 0.5, playerAbs.getY(), playerAbs.getZ() + 0.5, 0.0F, 90.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DWMItems.ASH_BOAT));

        var result = DWMItems.ASH_BOAT.use(context.getLevel(), player, InteractionHand.MAIN_HAND);
        if (!result.consumesAction()) {
            throw new AssertionError("Expected ash boat use on water to succeed, got " + result);
        }

        context.assertEntitiesPresent(DWMEntityTypes.ASH_BOAT, 1);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void signPlacementAndText(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);

        BlockPos floorRel = new BlockPos(2, 1, 2);
        context.setBlock(floorRel, Blocks.STONE);
        BlockPos standingRel = floorRel.above();
        placeItemOnBlock(context, player, DWMItems.ASH_SIGN, floorRel, Direction.UP);
        context.assertBlockPresent(DWMBlocks.ASH_SIGN, standingRel);
        assertSignText(context, standingRel, "Ash standing");

        BlockPos wallSupportRel = new BlockPos(5, 2, 2);
        context.setBlock(wallSupportRel, Blocks.STONE);
        BlockPos wallSignRel = wallSupportRel.west();
        placeItemOnBlock(context, player, DWMItems.ASH_SIGN, wallSupportRel, Direction.WEST);
        context.assertBlockPresent(DWMBlocks.ASH_WALL_SIGN, wallSignRel);
        assertSignText(context, wallSignRel, "Ash wall");

        BlockPos ceilingRel = new BlockPos(2, 5, 5);
        context.setBlock(ceilingRel, Blocks.STONE);
        BlockPos hangingRel = ceilingRel.below();
        placeItemOnBlock(context, player, DWMItems.ASH_HANGING_SIGN, ceilingRel, Direction.DOWN);
        context.assertBlockPresent(DWMBlocks.ASH_HANGING_SIGN, hangingRel);
        assertHangingSignText(context, hangingRel, "Ash hang");

        // Wall hanging signs attach between two solid supports on the attachment axis.
        BlockPos leftSupport = new BlockPos(4, 3, 5);
        BlockPos rightSupport = new BlockPos(6, 3, 5);
        BlockPos wallHangingRel = new BlockPos(5, 3, 5);
        context.setBlock(leftSupport, Blocks.STONE);
        context.setBlock(rightSupport, Blocks.STONE);
        placeItemOnBlock(context, player, DWMItems.ASH_HANGING_SIGN, leftSupport, Direction.EAST);
        context.assertBlockPresent(DWMBlocks.ASH_WALL_HANGING_SIGN, wallHangingRel);
        assertHangingSignText(context, wallHangingRel, "Ash wall hang");

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void saplingGeneratesAshTree(GameTestHelper context) {
        BlockPos dirtRel = new BlockPos(3, 0, 3);
        BlockPos saplingRel = dirtRel.above();
        context.setBlock(dirtRel, Blocks.DIRT);
        context.setBlock(saplingRel, DWMBlocks.ASH_SAPLING.defaultBlockState());

        ServerLevel world = context.getLevel();
        BlockPos saplingAbs = context.absolutePos(saplingRel);
        BlockState saplingState = world.getBlockState(saplingAbs);
        if (!(saplingState.getBlock() instanceof SaplingBlock sapling)) {
            throw new AssertionError("Expected ash sapling block");
        }

        boolean grew = false;
        for (long seed : List.of(1L, 2L, 3L, 7L, 13L, 42L, 99L)) {
            // STAGE 0 only advances; STAGE 1 triggers SaplingGenerator tree placement.
            world.setBlockAndUpdate(
                    saplingAbs,
                    DWMBlocks.ASH_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1)
            );
            sapling.advanceTree(world, saplingAbs, world.getBlockState(saplingAbs), RandomSource.create(seed));
            if (containsBlockInBox(context, DWMBlocks.ASH_LOG) && containsBlockInBox(context, DWMBlocks.ASH_LEAVES)) {
                grew = true;
                break;
            }
        }

        if (!grew) {
            throw new AssertionError("Expected ash sapling to generate ash log and ash leaves");
        }

        context.succeed();
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

        ItemStack result = craftingRecipe.assemble(input, world.registryAccess());
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

    private static void assertSelfDrop(GameTestHelper context, Player player, Block block, Item expected) {
        BlockPos pos = new BlockPos(1, 1, 1);
        context.setBlock(pos, block.defaultBlockState());
        assertDropsContain(context, player, pos, ItemStack.EMPTY, expected, 1);
    }

    private static void assertDropsContain(
            GameTestHelper context,
            Player player,
            BlockPos relativePos,
            ItemStack tool,
            Item expected,
            int count
    ) {
        List<ItemStack> drops = getDrops(context, player, relativePos, tool);
        assertHasItem(drops, expected, count, relativePos.toShortString());
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
        int actual = countItem(drops, expected);
        if (actual != count) {
            throw new AssertionError(
                    "Expected " + count + "x " + expected + " from " + label + " but got " + actual + " in " + drops
            );
        }
    }

    private static int countItem(List<ItemStack> drops, Item item) {
        int total = 0;
        for (ItemStack stack : drops) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void placeItemOnBlock(
            GameTestHelper context,
            Player player,
            Item item,
            BlockPos clickedRelative,
            Direction face
    ) {
        BlockPos clickedAbs = context.absolutePos(clickedRelative);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clickedAbs), face, clickedAbs, false);
        var result = item.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        if (!result.consumesAction()) {
            throw new AssertionError("Expected " + item + " placement on " + face + " to succeed, got " + result);
        }
    }

    private static void assertSignText(GameTestHelper context, BlockPos relativePos, String message) {
        BlockPos abs = context.absolutePos(relativePos);
        if (!(context.getLevel().getBlockEntity(abs) instanceof SignBlockEntity sign)) {
            throw new AssertionError("Expected SignBlockEntity at " + relativePos);
        }
        SignText text = new SignText().setMessage(0, Component.literal(message));
        if (!sign.setText(text, true)) {
            throw new AssertionError("Failed to set standing/wall sign text");
        }
        if (!message.equals(sign.getFrontText().getMessage(0, false).getString())) {
            throw new AssertionError("Sign text mismatch at " + relativePos);
        }
    }

    private static void assertHangingSignText(GameTestHelper context, BlockPos relativePos, String message) {
        BlockPos abs = context.absolutePos(relativePos);
        if (!(context.getLevel().getBlockEntity(abs) instanceof HangingSignBlockEntity sign)) {
            throw new AssertionError("Expected HangingSignBlockEntity at " + relativePos);
        }
        SignText text = new SignText().setMessage(0, Component.literal(message));
        if (!sign.setText(text, true)) {
            throw new AssertionError("Failed to set hanging sign text");
        }
        if (!message.equals(sign.getFrontText().getMessage(0, false).getString())) {
            throw new AssertionError("Hanging sign text mismatch at " + relativePos);
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
}
