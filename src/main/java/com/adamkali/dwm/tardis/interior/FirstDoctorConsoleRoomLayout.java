package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure First Doctor console-room block layout shared by world placement and client BOTI fallback.
 */
public final class FirstDoctorConsoleRoomLayout {
    public static final int SIZE_X = 11;
    public static final int SIZE_Y = 7;
    public static final int SIZE_Z = 11;

    /** Local entrance standing position relative to structure origin (feet). */
    public static final BlockPos LOCAL_ENTRANCE = new BlockPos(5, 1, 1);

    /** Layout version for client mesh cache invalidation. */
    public static final int LAYOUT_VERSION = 3;

    private static Map<BlockPos, BlockState> cachedPlacements;

    private FirstDoctorConsoleRoomLayout() {
    }

    public static Map<BlockPos, BlockState> placements() {
        if (cachedPlacements == null) {
            cachedPlacements = Collections.unmodifiableMap(buildPlacements());
        }
        return cachedPlacements;
    }

    /**
     * Blocks that should appear in the exterior BOTI preview (no air/light/interior doors).
     * Includes the First Doctor console (INVISIBLE + BER).
     */
    public static Map<BlockPos, BlockState> botiVisiblePlacements() {
        return BotiInteriorSampler.filterVisible(placements());
    }

    static Map<BlockPos, BlockState> buildPlacements() {
        Map<BlockPos, BlockState> placements = new HashMap<>();
        BlockState floor = DWMBlocks.WHITE_TARDIS_WALL.getDefaultState();
        BlockState wall = DWMBlocks.WHITE_ROUNDEL_A.getDefaultState();
        BlockState roundel = DWMBlocks.WHITE_BIG_ROUNDEL_A.getDefaultState();
        BlockState ceiling = DWMBlocks.LIGHT_GRAY_TARDIS_WALL.getDefaultState();
        BlockState console = DWMBlocks.FIRST_DOCTOR_CONSOLE.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState light = Blocks.LIGHT.getDefaultState();
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                placements.put(new BlockPos(x, 0, z), floor);
                placements.put(new BlockPos(x, SIZE_Y - 1, z), ceiling);
                for (int y = 1; y < SIZE_Y - 1; y++) {
                    boolean edge = x == 0 || x == SIZE_X - 1 || z == 0 || z == SIZE_Z - 1;
                    placements.put(new BlockPos(x, y, z), edge ? wall : air);
                }
            }
        }
        // 3×2 door bank: origin (4,1,0) = lower/slot0; slots increase east when facing south.
        Direction doorFacing = Direction.SOUTH;
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cell = TardisInteriorDoorBlock.cellPos(
                        new BlockPos(4, 1, 0), doorFacing, half, slot);
                placements.put(cell, TardisInteriorDoorBlock.bankCellState(doorFacing, half, slot, true));
            }
        }
        placements.put(new BlockPos(5, 1, 5), console);
        placements.put(new BlockPos(6, 1, 5), floor);
        placements.put(new BlockPos(4, 1, 5), floor);
        placements.put(new BlockPos(5, 1, 6), floor);
        placements.put(new BlockPos(5, 1, 4), floor);
        placements.put(new BlockPos(5, 4, 5), light);
        placements.put(new BlockPos(0, 2, 5), roundel);
        placements.put(new BlockPos(SIZE_X - 1, 2, 5), roundel);
        placements.put(new BlockPos(5, 2, SIZE_Z - 1), roundel);
        return placements;
    }
}
