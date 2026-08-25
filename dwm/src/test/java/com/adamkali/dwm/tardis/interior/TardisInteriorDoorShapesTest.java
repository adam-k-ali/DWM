package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisInteriorDoorShapesTest {
    private static final float EPSILON = 1e-3f;

    @Test
    void southFacingModelAabb_fillsThreeWideBank() {
        float[] aabb = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.SOUTH);
        // 3-wide mesh centered on 3-wide bank: 0..3 from origin.
        assertEquals(0.0f, aabb[0], EPSILON);
        assertEquals(0.0f, aabb[1], EPSILON);
        assertEquals(0.425f, aabb[2], EPSILON);
        assertEquals(3.0f, aabb[3], EPSILON);
        assertEquals(2.0f, aabb[4], EPSILON);
        assertEquals(1.0f, aabb[5], EPSILON);

        assertEquals(3.0f, aabb[3] - aabb[0], EPSILON);
        assertEquals(2.0f, aabb[4] - aabb[1], EPSILON);
    }

    @Test
    void eastWestModelAabb_matchesMinecraftYRotation() {
        // WEST (yaw -90°): bank offset goes along +Z from origin (north end of bank).
        float[] west = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.WEST);
        assertEquals(0.0f, west[0], EPSILON);
        assertEquals(0.0f, west[1], EPSILON);
        assertEquals(0.0f, west[2], EPSILON);
        assertEquals(0.575f, west[3], EPSILON);
        assertEquals(2.0f, west[4], EPSILON);
        assertEquals(3.0f, west[5], EPSILON);

        // EAST (yaw -270°): bank offset goes along -Z from origin (south end of bank).
        float[] east = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.EAST);
        assertEquals(0.425f, east[0], EPSILON);
        assertEquals(0.0f, east[1], EPSILON);
        assertEquals(-2.0f, east[2], EPSILON);
        assertEquals(1.0f, east[3], EPSILON);
        assertEquals(2.0f, east[4], EPSILON);
        assertEquals(1.0f, east[5], EPSILON);
    }

    @Test
    void eachBankCell_outlineIsClippedToUnitCube() {
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                VoxelShape shape = TardisInteriorDoorShapes.forCell(Direction.SOUTH, half, slot);
                assertFalse(shape.isEmpty(), "cell half=" + half + " slot=" + slot + " should intersect mesh");
                AABB local = shape.bounds();
                assertTrue(local.minX >= -EPSILON && local.minY >= -EPSILON && local.minZ >= -EPSILON);
                assertTrue(local.maxX <= 1.0 + EPSILON && local.maxY <= 1.0 + EPSILON && local.maxZ <= 1.0 + EPSILON);
            }
        }
    }

    @Test
    void originPos_fromUpperFarSlot_resolvesLowerSlotZero() {
        BlockPos far = new BlockPos(6, 2, 0);
        var state = TardisInteriorDoorBlock.bankCellState(
                Direction.SOUTH, DoubleBlockHalf.UPPER, 2, true);
        assertEquals(new BlockPos(4, 1, 0), TardisInteriorDoorBlock.originPos(far, state));
    }

    @Test
    void cellPos_southBankMatchesExpectedGrid() {
        BlockPos origin = new BlockPos(4, 1, 0);
        assertEquals(origin, TardisInteriorDoorBlock.cellPos(
                origin, Direction.SOUTH, DoubleBlockHalf.LOWER, 0));
        assertEquals(new BlockPos(6, 2, 0), TardisInteriorDoorBlock.cellPos(
                origin, Direction.SOUTH, DoubleBlockHalf.UPPER, 2));
    }
}
