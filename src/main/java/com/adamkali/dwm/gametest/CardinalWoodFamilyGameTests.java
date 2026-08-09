package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.TallDoorBlock;
import com.adamkali.dwm.block.wood.TallDoorSegment;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardinalWoodFamilyGameTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void craftingRecipesProduceExpectedOutputs(TestContext context) {
        assertCrafts(context, "cardinal_planks", grid(1, 1, DWMBlocks.CARDINAL_LOG), DWMBlocks.CARDINAL_PLANKS.asItem(), 4);
        assertCrafts(context, "cardinal_planks", grid(1, 1, DWMBlocks.STRIPPED_CARDINAL_WOOD), DWMBlocks.CARDINAL_PLANKS.asItem(), 4);

        assertCrafts(context, "cardinal_wood", grid(2, 2,
                DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LOG,
                DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LOG), DWMBlocks.CARDINAL_WOOD.asItem(), 3);
        assertCrafts(context, "stripped_cardinal_wood", grid(2, 2,
                DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG,
                DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG), DWMBlocks.STRIPPED_CARDINAL_WOOD.asItem(), 3);

        assertCrafts(context, "cardinal_stairs", grid(3, 3,
                DWMBlocks.CARDINAL_PLANKS, Items.AIR, Items.AIR,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, Items.AIR,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS), DWMBlocks.CARDINAL_STAIRS.asItem(), 4);
        assertCrafts(context, "cardinal_slab", grid(3, 1,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS), DWMBlocks.CARDINAL_SLAB.asItem(), 6);
        assertCrafts(context, "cardinal_fence", grid(3, 2,
                DWMBlocks.CARDINAL_PLANKS, Items.STICK, DWMBlocks.CARDINAL_PLANKS,
                DWMBlocks.CARDINAL_PLANKS, Items.STICK, DWMBlocks.CARDINAL_PLANKS), DWMBlocks.CARDINAL_FENCE.asItem(), 3);
        assertCrafts(context, "cardinal_fence_gate", grid(3, 2,
                Items.STICK, DWMBlocks.CARDINAL_PLANKS, Items.STICK,
                Items.STICK, DWMBlocks.CARDINAL_PLANKS, Items.STICK), DWMBlocks.CARDINAL_FENCE_GATE.asItem(), 1);
        assertCrafts(context, "cardinal_pressure_plate", grid(2, 1,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS), DWMBlocks.CARDINAL_PRESSURE_PLATE.asItem(), 1);
        assertCrafts(context, "cardinal_button", grid(1, 1, DWMBlocks.CARDINAL_PLANKS), DWMBlocks.CARDINAL_BUTTON.asItem(), 1);
        assertCrafts(context, "cardinal_sign", grid(3, 3,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS,
                Items.AIR, Items.STICK, Items.AIR), DWMItems.CARDINAL_SIGN, 3);
        assertCrafts(context, "cardinal_boat", grid(3, 2,
                DWMBlocks.CARDINAL_PLANKS, Items.AIR, DWMBlocks.CARDINAL_PLANKS,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS), DWMItems.CARDINAL_BOAT, 1);
        assertCrafts(context, "cardinal_hanging_sign", grid(3, 3,
                Items.CHAIN, Items.AIR, Items.CHAIN,
                DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG,
                DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG),
                DWMItems.CARDINAL_HANGING_SIGN, 6);
        assertCrafts(context, "cardinal_trapdoor", grid(3, 2,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS),
                DWMBlocks.CARDINAL_TRAPDOOR.asItem(), 2);
        assertCrafts(context, "cardinal_door", grid(3, 3,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, Items.AIR,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, Items.AIR,
                DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS, Items.AIR), DWMBlocks.CARDINAL_DOOR.asItem(), 3);

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void blockLootDropsExpectedItems(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);

        assertSelfDrop(context, player, DWMBlocks.CARDINAL_PLANKS, DWMBlocks.CARDINAL_PLANKS.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_LOG.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_WOOD, DWMBlocks.CARDINAL_WOOD.asItem());
        assertSelfDrop(context, player, DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_LOG.asItem());
        assertSelfDrop(context, player, DWMBlocks.STRIPPED_CARDINAL_WOOD, DWMBlocks.STRIPPED_CARDINAL_WOOD.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_STAIRS, DWMBlocks.CARDINAL_STAIRS.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_FENCE, DWMBlocks.CARDINAL_FENCE.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_FENCE_GATE, DWMBlocks.CARDINAL_FENCE_GATE.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_BUTTON, DWMBlocks.CARDINAL_BUTTON.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_PRESSURE_PLATE, DWMBlocks.CARDINAL_PRESSURE_PLATE.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_TRAPDOOR, DWMBlocks.CARDINAL_TRAPDOOR.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_DOOR, DWMBlocks.CARDINAL_DOOR.asItem());
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_SIGN, DWMItems.CARDINAL_SIGN);
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_HANGING_SIGN, DWMItems.CARDINAL_HANGING_SIGN);
        assertSelfDrop(context, player, DWMBlocks.CARDINAL_SAPLING, DWMBlocks.CARDINAL_SAPLING.asItem());

        BlockPos slabPos = new BlockPos(1, 1, 1);
        context.setBlockState(slabPos, DWMBlocks.CARDINAL_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM));
        assertDropsContain(context, player, slabPos, ItemStack.EMPTY, DWMBlocks.CARDINAL_SLAB.asItem(), 1);

        context.setBlockState(slabPos, DWMBlocks.CARDINAL_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE));
        assertDropsContain(context, player, slabPos, ItemStack.EMPTY, DWMBlocks.CARDINAL_SLAB.asItem(), 2);

        BlockPos wallSignPos = new BlockPos(2, 1, 1);
        context.setBlockState(wallSignPos, DWMBlocks.CARDINAL_WALL_SIGN.getDefaultState());
        assertDropsContain(context, player, wallSignPos, ItemStack.EMPTY, DWMItems.CARDINAL_SIGN, 1);

        BlockPos wallHangingPos = new BlockPos(3, 1, 1);
        context.setBlockState(wallHangingPos, DWMBlocks.CARDINAL_WALL_HANGING_SIGN.getDefaultState());
        assertDropsContain(context, player, wallHangingPos, ItemStack.EMPTY, DWMItems.CARDINAL_HANGING_SIGN, 1);

        BlockPos pottedPos = new BlockPos(4, 1, 1);
        context.setBlockState(pottedPos, DWMBlocks.POTTED_CARDINAL_SAPLING.getDefaultState());
        List<ItemStack> pottedDrops = getDrops(context, player, pottedPos, ItemStack.EMPTY);
        assertHasItem(pottedDrops, Items.FLOWER_POT, 1, "potted cardinal sapling");
        assertHasItem(pottedDrops, DWMBlocks.CARDINAL_SAPLING.asItem(), 1, "potted cardinal sapling");

        BlockPos leavesPos = new BlockPos(5, 1, 1);
        context.setBlockState(leavesPos, DWMBlocks.CARDINAL_LEAVES.getDefaultState());
        List<ItemStack> shearsDrops = getDrops(context, player, leavesPos, new ItemStack(Items.SHEARS));
        assertHasItem(shearsDrops, DWMBlocks.CARDINAL_LEAVES.asItem(), 1, "cardinal leaves with shears");

        List<ItemStack> bareDrops = getDrops(context, player, leavesPos, ItemStack.EMPTY);
        if (countItem(bareDrops, DWMBlocks.CARDINAL_LEAVES.asItem()) > 0) {
            throw new AssertionError("Expected cardinal leaves without shears/silk touch not to drop leaves");
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void orientedBlocksAcceptSupportedDirections(TestContext context) {
        for (Block log : List.of(
                DWMBlocks.CARDINAL_LOG, DWMBlocks.CARDINAL_WOOD, DWMBlocks.STRIPPED_CARDINAL_LOG, DWMBlocks.STRIPPED_CARDINAL_WOOD)) {
            for (Direction.Axis axis : Direction.Axis.values()) {
                BlockPos pos = new BlockPos(1 + axis.ordinal(), 1, 1);
                context.setBlockState(pos, log.getDefaultState().with(PillarBlock.AXIS, axis));
                context.expectBlockProperty(pos, PillarBlock.AXIS, axis);
            }
        }

        int stairX = 1;
        for (Direction facing : Direction.Type.HORIZONTAL) {
            BlockPos pos = new BlockPos(stairX++, 2, 2);
            context.setBlockState(pos, DWMBlocks.CARDINAL_STAIRS.getDefaultState().with(StairsBlock.FACING, facing));
            context.expectBlockProperty(pos, StairsBlock.FACING, facing);
        }

        BlockPos bottomSlab = new BlockPos(1, 3, 1);
        context.setBlockState(bottomSlab, DWMBlocks.CARDINAL_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM));
        context.expectBlockProperty(bottomSlab, SlabBlock.TYPE, SlabType.BOTTOM);
        BlockPos topSlab = new BlockPos(2, 3, 1);
        context.setBlockState(topSlab, DWMBlocks.CARDINAL_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP));
        context.expectBlockProperty(topSlab, SlabBlock.TYPE, SlabType.TOP);
        BlockPos doubleSlab = new BlockPos(3, 3, 1);
        context.setBlockState(doubleSlab, DWMBlocks.CARDINAL_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE));
        context.expectBlockProperty(doubleSlab, SlabBlock.TYPE, SlabType.DOUBLE);

        context.setBlockState(1, 3, 4, Blocks.STONE);
        BlockPos floorButton = new BlockPos(1, 4, 4);
        context.setBlockState(floorButton, DWMBlocks.CARDINAL_BUTTON.getDefaultState()
                .with(WallMountedBlock.FACE, BlockFace.FLOOR)
                .with(ButtonBlock.FACING, Direction.NORTH));
        context.expectBlockProperty(floorButton, WallMountedBlock.FACE, BlockFace.FLOOR);

        context.setBlockState(2, 4, 4, Blocks.STONE);
        BlockPos wallButton = new BlockPos(3, 4, 4);
        context.setBlockState(wallButton, DWMBlocks.CARDINAL_BUTTON.getDefaultState()
                .with(WallMountedBlock.FACE, BlockFace.WALL)
                .with(ButtonBlock.FACING, Direction.EAST));
        context.expectBlockProperty(wallButton, WallMountedBlock.FACE, BlockFace.WALL);

        context.setBlockState(5, 5, 4, Blocks.STONE);
        BlockPos ceilingButton = new BlockPos(5, 4, 4);
        context.setBlockState(ceilingButton, DWMBlocks.CARDINAL_BUTTON.getDefaultState()
                .with(WallMountedBlock.FACE, BlockFace.CEILING)
                .with(ButtonBlock.FACING, Direction.SOUTH));
        context.expectBlockProperty(ceilingButton, WallMountedBlock.FACE, BlockFace.CEILING);

        int gateX = 1;
        for (Direction facing : Direction.Type.HORIZONTAL) {
            BlockPos pos = new BlockPos(gateX++, 5, 3);
            context.setBlockState(pos, DWMBlocks.CARDINAL_FENCE_GATE.getDefaultState().with(FenceGateBlock.FACING, facing));
            context.expectBlockProperty(pos, FenceGateBlock.FACING, facing);
        }

        context.setBlockState(5, 5, 5, Blocks.STONE);
        context.setBlockState(5, 6, 5, DWMBlocks.CARDINAL_PRESSURE_PLATE.getDefaultState());
        context.expectBlock(DWMBlocks.CARDINAL_PRESSURE_PLATE, 5, 6, 5);

        int trapX = 1;
        for (Direction facing : Direction.Type.HORIZONTAL) {
            BlockPos pos = new BlockPos(trapX++, 7, 2);
            context.setBlockState(pos, DWMBlocks.CARDINAL_TRAPDOOR.getDefaultState()
                    .with(net.minecraft.block.TrapdoorBlock.FACING, facing)
                    .with(net.minecraft.block.TrapdoorBlock.OPEN, false));
            context.expectBlockProperty(pos, net.minecraft.block.TrapdoorBlock.FACING, facing);
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void tallDoorPlacesTogglesAndRespondsToRedstone(TestContext context) {
        context.setBlockState(2, 0, 2, Blocks.STONE);
        BlockPos origin = new BlockPos(2, 1, 2);
        BlockState base = DWMBlocks.CARDINAL_DOOR.getDefaultState()
                .with(TallDoorBlock.FACING, Direction.NORTH)
                .with(TallDoorBlock.HINGE, DoorHinge.LEFT)
                .with(TallDoorBlock.OPEN, false)
                .with(TallDoorBlock.POWERED, false);
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            context.setBlockState(origin.up(segment.index()), base.with(TallDoorBlock.SEGMENT, segment));
        }

        context.expectBlock(DWMBlocks.CARDINAL_DOOR, origin);
        context.expectBlock(DWMBlocks.CARDINAL_DOOR, origin.up());
        context.expectBlock(DWMBlocks.CARDINAL_DOOR, origin.up(2));
        context.expectBlockProperty(origin, TallDoorBlock.SEGMENT, TallDoorSegment.BOTTOM);
        context.expectBlockProperty(origin.up(), TallDoorBlock.SEGMENT, TallDoorSegment.MIDDLE);
        context.expectBlockProperty(origin.up(2), TallDoorBlock.SEGMENT, TallDoorSegment.TOP);

        context.useBlock(origin);
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            context.expectBlockProperty(origin.up(segment.index()), TallDoorBlock.OPEN, true);
            context.expectBlockProperty(origin.up(segment.index()), TallDoorBlock.POWERED, false);
        }

        context.setBlockState(origin.east(), Blocks.REDSTONE_BLOCK);
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            context.expectBlockProperty(origin.up(segment.index()), TallDoorBlock.OPEN, true);
            context.expectBlockProperty(origin.up(segment.index()), TallDoorBlock.POWERED, true);
        }

        context.setBlockState(origin.east(), Blocks.AIR);
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            context.expectBlockProperty(origin.up(segment.index()), TallDoorBlock.OPEN, false);
            context.expectBlockProperty(origin.up(segment.index()), TallDoorBlock.POWERED, false);
        }

        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        List<ItemStack> bottomDrops = getDrops(context, player, origin, ItemStack.EMPTY);
        assertHasItem(bottomDrops, DWMBlocks.CARDINAL_DOOR.asItem(), 1, "tall door bottom");
        List<ItemStack> middleDrops = getDrops(context, player, origin.up(), ItemStack.EMPTY);
        if (countItem(middleDrops, DWMBlocks.CARDINAL_DOOR.asItem()) != 0) {
            throw new AssertionError("Middle tall-door segment must not drop an item");
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void boatPlacesOnWater(TestContext context) {
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                context.setBlockState(x, 0, z, Blocks.STONE);
                context.setBlockState(x, 1, z, Blocks.WATER);
            }
        }

        BlockPos playerRel = new BlockPos(3, 3, 3);
        BlockPos playerAbs = context.getAbsolutePos(playerRel);
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        player.refreshPositionAndAngles(playerAbs.getX() + 0.5, playerAbs.getY(), playerAbs.getZ() + 0.5, 0.0F, 90.0F);
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(DWMItems.CARDINAL_BOAT));

        var result = DWMItems.CARDINAL_BOAT.use(context.getWorld(), player, Hand.MAIN_HAND);
        if (!result.isAccepted()) {
            throw new AssertionError("Expected cardinal boat use on water to succeed, got " + result);
        }

        context.expectEntities(DWMEntityTypes.CARDINAL_BOAT, 1);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void signPlacementAndText(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);

        BlockPos floorRel = new BlockPos(2, 1, 2);
        context.setBlockState(floorRel, Blocks.STONE);
        BlockPos standingRel = floorRel.up();
        placeItemOnBlock(context, player, DWMItems.CARDINAL_SIGN, floorRel, Direction.UP);
        context.expectBlock(DWMBlocks.CARDINAL_SIGN, standingRel);
        assertSignText(context, standingRel, "Cardinal standing");

        BlockPos wallSupportRel = new BlockPos(5, 2, 2);
        context.setBlockState(wallSupportRel, Blocks.STONE);
        BlockPos wallSignRel = wallSupportRel.west();
        placeItemOnBlock(context, player, DWMItems.CARDINAL_SIGN, wallSupportRel, Direction.WEST);
        context.expectBlock(DWMBlocks.CARDINAL_WALL_SIGN, wallSignRel);
        assertSignText(context, wallSignRel, "Cardinal wall");

        BlockPos ceilingRel = new BlockPos(2, 5, 5);
        context.setBlockState(ceilingRel, Blocks.STONE);
        BlockPos hangingRel = ceilingRel.down();
        placeItemOnBlock(context, player, DWMItems.CARDINAL_HANGING_SIGN, ceilingRel, Direction.DOWN);
        context.expectBlock(DWMBlocks.CARDINAL_HANGING_SIGN, hangingRel);
        assertHangingSignText(context, hangingRel, "Cardinal hang");

        // Wall hanging signs attach between two solid supports on the attachment axis.
        BlockPos leftSupport = new BlockPos(4, 3, 5);
        BlockPos rightSupport = new BlockPos(6, 3, 5);
        BlockPos wallHangingRel = new BlockPos(5, 3, 5);
        context.setBlockState(leftSupport, Blocks.STONE);
        context.setBlockState(rightSupport, Blocks.STONE);
        placeItemOnBlock(context, player, DWMItems.CARDINAL_HANGING_SIGN, leftSupport, Direction.EAST);
        context.expectBlock(DWMBlocks.CARDINAL_WALL_HANGING_SIGN, wallHangingRel);
        assertHangingSignText(context, wallHangingRel, "Dark Cardinal wall hang");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void saplingGeneratesDarkAshTree(TestContext context) {
        BlockPos dirtRel = new BlockPos(3, 0, 3);
        BlockPos saplingRel = dirtRel.up();
        context.setBlockState(dirtRel, Blocks.DIRT);
        context.setBlockState(saplingRel, DWMBlocks.CARDINAL_SAPLING.getDefaultState());

        ServerWorld world = context.getWorld();
        BlockPos saplingAbs = context.getAbsolutePos(saplingRel);
        BlockState saplingState = world.getBlockState(saplingAbs);
        if (!(saplingState.getBlock() instanceof SaplingBlock sapling)) {
            throw new AssertionError("Expected cardinal sapling block");
        }

        boolean grew = false;
        for (long seed : List.of(1L, 2L, 3L, 7L, 13L, 42L, 99L)) {
            // STAGE 0 only advances; STAGE 1 triggers SaplingGenerator tree placement.
            world.setBlockState(
                    saplingAbs,
                    DWMBlocks.CARDINAL_SAPLING.getDefaultState().with(SaplingBlock.STAGE, 1)
            );
            sapling.generate(world, saplingAbs, world.getBlockState(saplingAbs), Random.create(seed));
            if (containsBlockInBox(context, DWMBlocks.CARDINAL_LOG) && containsBlockInBox(context, DWMBlocks.CARDINAL_LEAVES)) {
                grew = true;
                break;
            }
        }

        if (!grew) {
            throw new AssertionError("Expected cardinal sapling to generate cardinal log and cardinal leaves");
        }

        context.complete();
    }

    private static void assertCrafts(
            TestContext context,
            String recipePath,
            CraftingRecipeInput input,
            Item expected,
            int count
    ) {
        ServerWorld world = context.getWorld();
        ServerRecipeManager recipes = world.getServer().getRecipeManager();
        RegistryKey<Recipe<?>> key = RegistryKey.of(
                RegistryKeys.RECIPE,
                Identifier.of(DWMReference.MOD_ID, recipePath)
        );
        Optional<RecipeEntry<?>> byId = recipes.get(key);
        if (byId.isEmpty()) {
            throw new AssertionError("Missing recipe dwm:" + recipePath);
        }
        if (!(byId.get().value() instanceof CraftingRecipe craftingRecipe)) {
            throw new AssertionError("Recipe dwm:" + recipePath + " is not a crafting recipe");
        }
        if (!craftingRecipe.matches(input, world)) {
            throw new AssertionError("Recipe dwm:" + recipePath + " did not match crafted input");
        }

        Optional<RecipeEntry<CraftingRecipe>> match = recipes.getFirstMatch(RecipeType.CRAFTING, input, world);
        if (match.isEmpty()) {
            throw new AssertionError("No crafting match for dwm:" + recipePath);
        }

        ItemStack result = craftingRecipe.craft(input, world.getRegistryManager());
        if (!result.isOf(expected) || result.getCount() != count) {
            throw new AssertionError(
                    "Recipe dwm:" + recipePath + " expected " + count + "x " + expected
                            + " but got " + result.getCount() + "x " + result.getItem()
            );
        }
    }

    private static CraftingRecipeInput grid(int width, int height, Object... cells) {
        if (cells.length != width * height) {
            throw new IllegalArgumentException("Grid size mismatch");
        }
        List<ItemStack> stacks = new ArrayList<>(cells.length);
        for (Object cell : cells) {
            stacks.add(stackOf(cell));
        }
        return CraftingRecipeInput.create(width, height, stacks);
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

    private static void assertSelfDrop(TestContext context, PlayerEntity player, Block block, Item expected) {
        BlockPos pos = new BlockPos(1, 1, 1);
        context.setBlockState(pos, block.getDefaultState());
        assertDropsContain(context, player, pos, ItemStack.EMPTY, expected, 1);
    }

    private static void assertDropsContain(
            TestContext context,
            PlayerEntity player,
            BlockPos relativePos,
            ItemStack tool,
            Item expected,
            int count
    ) {
        List<ItemStack> drops = getDrops(context, player, relativePos, tool);
        assertHasItem(drops, expected, count, relativePos.toShortString());
    }

    private static List<ItemStack> getDrops(
            TestContext context,
            PlayerEntity player,
            BlockPos relativePos,
            ItemStack tool
    ) {
        BlockPos abs = context.getAbsolutePos(relativePos);
        ServerWorld world = context.getWorld();
        BlockState state = world.getBlockState(abs);
        return Block.getDroppedStacks(state, world, abs, world.getBlockEntity(abs), player, tool);
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
            if (stack.isOf(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void placeItemOnBlock(
            TestContext context,
            PlayerEntity player,
            Item item,
            BlockPos clickedRelative,
            Direction face
    ) {
        BlockPos clickedAbs = context.getAbsolutePos(clickedRelative);
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(item));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(clickedAbs), face, clickedAbs, false);
        var result = item.useOnBlock(new net.minecraft.item.ItemUsageContext(player, Hand.MAIN_HAND, hit));
        if (!result.isAccepted()) {
            throw new AssertionError("Expected " + item + " placement on " + face + " to succeed, got " + result);
        }
    }

    private static void assertSignText(TestContext context, BlockPos relativePos, String message) {
        BlockPos abs = context.getAbsolutePos(relativePos);
        if (!(context.getWorld().getBlockEntity(abs) instanceof SignBlockEntity sign)) {
            throw new AssertionError("Expected SignBlockEntity at " + relativePos);
        }
        SignText text = new SignText().withMessage(0, Text.literal(message));
        if (!sign.setText(text, true)) {
            throw new AssertionError("Failed to set standing/wall sign text");
        }
        if (!message.equals(sign.getFrontText().getMessage(0, false).getString())) {
            throw new AssertionError("Sign text mismatch at " + relativePos);
        }
    }

    private static void assertHangingSignText(TestContext context, BlockPos relativePos, String message) {
        BlockPos abs = context.getAbsolutePos(relativePos);
        if (!(context.getWorld().getBlockEntity(abs) instanceof HangingSignBlockEntity sign)) {
            throw new AssertionError("Expected HangingSignBlockEntity at " + relativePos);
        }
        SignText text = new SignText().withMessage(0, Text.literal(message));
        if (!sign.setText(text, true)) {
            throw new AssertionError("Failed to set hanging sign text");
        }
        if (!message.equals(sign.getFrontText().getMessage(0, false).getString())) {
            throw new AssertionError("Hanging sign text mismatch at " + relativePos);
        }
    }

    private static boolean containsBlockInBox(TestContext context, Block block) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    if (context.getWorld().getBlockState(context.getAbsolutePos(new BlockPos(x, y, z))).isOf(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
