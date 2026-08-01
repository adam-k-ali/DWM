package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.function.Predicate;

/**
 * Chooses a single cell in a contiguous interior-door bank to own the BER mesh,
 * so a 3×2 (or similar) footprint does not draw the full double-door model repeatedly.
 *
 * <p>Primary = bottom-most, then the cell with no matching door toward
 * {@code facing.rotateYClockwise()} (west when facing south).
 */
public final class TardisInteriorDoorRenderAnchor {
    private TardisInteriorDoorRenderAnchor() {
    }

    public static boolean isPrimary(BlockView world, BlockPos pos, Direction facing) {
        return isPrimary(pos, facing, neighbor -> isMatchingDoor(world.getBlockState(neighbor), facing));
    }

    /**
     * Pure variant for unit tests: {@code isMatchingDoor} is true for neighbor cells that
     * belong to the same facing door bank.
     */
    public static boolean isPrimary(BlockPos pos, Direction facing, Predicate<BlockPos> isMatchingDoor) {
        if (isMatchingDoor.test(pos.down())) {
            return false;
        }
        Direction towardBankStart = facing.rotateYClockwise();
        return !isMatchingDoor.test(pos.offset(towardBankStart));
    }

    private static boolean isMatchingDoor(BlockState state, Direction facing) {
        return state.isOf(DWMBlocks.TARDIS_INTERIOR_DOOR)
                && state.get(TardisInteriorDoorBlock.FACING) == facing;
    }
}
