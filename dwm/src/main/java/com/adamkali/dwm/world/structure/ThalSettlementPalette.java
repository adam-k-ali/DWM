package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Block palette of the Thal settlement. Walls, frames and roofs use DWM petrified building blocks; paths
 * use vanilla dirt path. Everything block-specific lives here so pieces stay layout-only.
 */
public final class ThalSettlementPalette {
    /**
     * The crop planted in settlement fields. Placeholder until the Thal crop of DWM-068 exists; swap this one
     * constant (it must be a {@link CropBlock}) and the fields follow.
     */
    public static final CropBlock FIELD_CROP = (CropBlock) Blocks.WHEAT;
    /** Lowest crop growth stage planted, so fields show a mix of young and ripe plants. */
    public static final int FIELD_CROP_MIN_AGE = 2;

    private ThalSettlementPalette() {
    }

    public static BlockState planks() {
        return DWMBlocks.PETRIFIED_PLANKS.defaultBlockState();
    }

    /** Frame posts and corner pillars. */
    public static BlockState post() {
        return DWMBlocks.STRIPPED_PETRIFIED_LOG.defaultBlockState();
    }

    public static BlockState slab() {
        return DWMBlocks.PETRIFIED_SLAB.defaultBlockState();
    }

    public static BlockState wall() {
        return DWMBlocks.PETRIFIED_WALL.defaultBlockState();
    }

    /** Roof / seat stairs; {@code ascending} is the side the raised step is on. */
    public static BlockState stairs(Direction ascending) {
        return DWMBlocks.PETRIFIED_STAIRS.defaultBlockState().setValue(StairBlock.FACING, ascending);
    }

    public static BlockState path() {
        return Blocks.DIRT_PATH.defaultBlockState();
    }

    /** Fill under floors and paths so nothing floats over a dip in the terrain. */
    public static BlockState foundation() {
        return Blocks.DIRT.defaultBlockState();
    }

    public static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }

    public static BlockState window() {
        return Blocks.GLASS.defaultBlockState();
    }

    public static BlockState farmland() {
        return Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE);
    }

    public static BlockState water() {
        return Blocks.WATER.defaultBlockState();
    }

    public static BlockState crop(int age) {
        return FIELD_CROP.getStateForAge(Math.min(age, FIELD_CROP.getMaxAge()));
    }

    public static BlockState door(Direction facing, DoubleBlockHalf half) {
        return Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, half);
    }

    public static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    public static BlockState hangingLantern() {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    public static BlockState bed(Direction headDirection, BedPart part) {
        return Blocks.BED.brown().defaultBlockState().setValue(BedBlock.FACING, headDirection).setValue(BedBlock.PART, part);
    }

    public static BlockState craftingTable() {
        return Blocks.CRAFTING_TABLE.defaultBlockState();
    }

    public static BlockState barrel() {
        return Blocks.BARREL.defaultBlockState();
    }
}
