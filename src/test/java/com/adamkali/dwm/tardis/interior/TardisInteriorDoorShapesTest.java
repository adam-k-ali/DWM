package com.adamkali.dwm.tardis.interior;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisInteriorDoorShapesTest {
    private static final float EPSILON = 1e-3f;

    @Test
    void southFacingModelAabb_fillsThreeWideBank() {
        float[] aabb = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.SOUTH);
        // 3-wide mesh centered on 3-wide bank: 0..3 from primary.
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
        // WEST (yaw -90°): bank offset goes along +Z from primary (north end of bank).
        float[] west = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.WEST);
        assertEquals(0.0f, west[0], EPSILON);
        assertEquals(0.0f, west[1], EPSILON);
        assertEquals(0.0f, west[2], EPSILON);
        assertEquals(0.575f, west[3], EPSILON);
        assertEquals(2.0f, west[4], EPSILON);
        assertEquals(3.0f, west[5], EPSILON);

        // EAST (yaw -270°): bank offset goes along -Z from primary (south end of bank).
        float[] east = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.EAST);
        assertEquals(0.425f, east[0], EPSILON);
        assertEquals(0.0f, east[1], EPSILON);
        assertEquals(-2.0f, east[2], EPSILON);
        assertEquals(1.0f, east[3], EPSILON);
        assertEquals(2.0f, east[4], EPSILON);
        assertEquals(1.0f, east[5], EPSILON);
    }

    @Test
    void anyBankCell_outlineSpansFullDoorInWorldSpace() {
        Set<BlockPos> bank = new HashSet<>();
        for (int y = 1; y <= 2; y++) {
            for (int x = 4; x <= 6; x++) {
                bank.add(new BlockPos(x, y, 0));
            }
        }

        BlockPos primary = new BlockPos(4, 1, 0);
        float[] model = TardisInteriorDoorShapes.modelAabbRelativeToPrimary(Direction.SOUTH);

        for (BlockPos pos : bank) {
            VoxelShape shape = TardisInteriorDoorShapes.forCell(Direction.SOUTH, pos, bank::contains);
            Box local = shape.getBoundingBox();
            int dx = pos.getX() - primary.getX();
            int dy = pos.getY() - primary.getY();
            int dz = pos.getZ() - primary.getZ();

            // Cell-local box + cell offset == primary-relative model AABB (full 3×2 door).
            assertEquals(model[0], local.minX + dx, EPSILON);
            assertEquals(model[1], local.minY + dy, EPSILON);
            assertEquals(model[2], local.minZ + dz, EPSILON);
            assertEquals(model[3], local.maxX + dx, EPSILON);
            assertEquals(model[4], local.maxY + dy, EPSILON);
            assertEquals(model[5], local.maxZ + dz, EPSILON);

            // Extends outside the unit cube (not a single-block slab).
            assertTrue(local.maxX - local.minX > 1.0 + EPSILON || local.minX < -EPSILON || local.maxX > 1.0 + EPSILON
                    || local.maxY - local.minY > 1.0 + EPSILON || local.minY < -EPSILON || local.maxY > 1.0 + EPSILON);
        }
    }

    @Test
    void findPrimary_matchesRenderAnchor() {
        Set<BlockPos> bank = new HashSet<>();
        for (int y = 1; y <= 2; y++) {
            for (int x = 4; x <= 6; x++) {
                bank.add(new BlockPos(x, y, 0));
            }
        }
        assertEquals(
                new BlockPos(4, 1, 0),
                TardisInteriorDoorShapes.findPrimary(new BlockPos(6, 2, 0), Direction.SOUTH, bank::contains));
    }
}
