package com.adamkali.dwm.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisDecorBlockTest {
    private static final double EPSILON = 1e-4;

    @Test
    void placementFacing_matchesPlayerLookNotOpposite() {
        // Regression: getOpposite() made chairs/scanners face away from the player.
        assertEquals(Direction.NORTH, TardisDecorShapes.facingForPlacement(Direction.NORTH));
        assertEquals(Direction.SOUTH, TardisDecorShapes.facingForPlacement(Direction.SOUTH));
        assertEquals(Direction.EAST, TardisDecorShapes.facingForPlacement(Direction.EAST));
        assertNotEquals(Direction.WEST.getOpposite(), TardisDecorShapes.facingForPlacement(Direction.WEST));
    }

    @Test
    void smallChairShape_isNonEmptyAndTallerThanOneBlock() {
        AABB bounds = TardisDecorShapes.SMALL_CHAIR.bounds();
        assertTrue(bounds.getYsize() > 1.0);
        assertTrue(bounds.getXsize() > 0.5);
        assertTrue(bounds.getZsize() > 0.5);
    }

    @Test
    void largeChairShape_tallerThanSmallChair() {
        assertTrue(TardisDecorShapes.LARGE_CHAIR.bounds().getYsize()
                > TardisDecorShapes.SMALL_CHAIR.bounds().getYsize());
    }

    @Test
    void columnShape_isCentered() {
        AABB bounds = TardisDecorShapes.COLUMN.bounds();
        assertEquals(0.25, bounds.minX, EPSILON);
        assertEquals(0.75, bounds.maxX, EPSILON);
        assertEquals(0.25, bounds.minZ, EPSILON);
        assertEquals(0.75, bounds.maxZ, EPSILON);
        assertTrue(bounds.getYsize() > 1.5);
    }

    @Test
    void fullScanner_rotatesWithFacing() {
        VoxelShape north = TardisDecorShapes.FULL_SCANNER_NORTH;
        VoxelShape east = TardisDecorShapes.rotateHorizontal(north, Direction.EAST);
        AABB northBounds = north.bounds();
        AABB eastBounds = east.bounds();
        // North: wide in X; East: wide in Z
        assertTrue(northBounds.getXsize() > northBounds.getZsize());
        assertTrue(eastBounds.getZsize() > eastBounds.getXsize());
        assertEquals(northBounds.getYsize(), eastBounds.getYsize(), EPSILON);
    }

    @Test
    void globeAndCompactScanner_haveExpectedHeights() {
        assertTrue(TardisDecorShapes.GLOBE.bounds().getYsize() > 2.0);
        assertEquals(2.0, TardisDecorShapes.COMPACT_SCANNER.bounds().getYsize(), EPSILON);
    }
}
