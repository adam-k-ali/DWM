package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArsGridTest {
    private static final int WIDTH = 3;
    private static final int DEPTH = 2;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void rotationIndicesMatchVanillaOrder() {
        Rotation[] values = Rotation.values();
        assertEquals(ArsGrid.ROTATION_COUNT, values.length);
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], Rotation.BY_ID.apply(i));
        }
        assertEquals(Rotation.CLOCKWISE_90, values[1]);
        assertEquals(Rotation.CLOCKWISE_180, values[2]);
        assertEquals(Rotation.COUNTERCLOCKWISE_90, values[3]);
    }

    /**
     * Cross-checks {@link ArsGrid#rotateCell} against vanilla {@link StructureTemplate#transform}: rotate
     * each unrotated cell's centre block about the origin, shift by the rotated bounding box min
     * corner, and convert back to cells.
     */
    @Test
    void rotateCellMatchesStructureTemplateTransformForNonSquareFootprint() {
        int sizeX = WIDTH * ArsGrid.CELL_SIZE;
        int sizeZ = DEPTH * ArsGrid.CELL_SIZE;
        for (Rotation rotation : Rotation.values()) {
            int r = rotation.ordinal();
            BlockPos cornerA = StructureTemplate.transform(BlockPos.ZERO, Mirror.NONE, rotation, BlockPos.ZERO);
            BlockPos cornerB = StructureTemplate.transform(
                    new BlockPos(sizeX - 1, 0, sizeZ - 1), Mirror.NONE, rotation, BlockPos.ZERO);
            int minX = Math.min(cornerA.getX(), cornerB.getX());
            int minZ = Math.min(cornerA.getZ(), cornerB.getZ());
            int rotatedSizeX = Math.abs(cornerA.getX() - cornerB.getX()) + 1;
            int rotatedSizeZ = Math.abs(cornerA.getZ() - cornerB.getZ()) + 1;

            assertEquals(ArsGrid.rotatedWidth(WIDTH, DEPTH, r) * ArsGrid.CELL_SIZE, rotatedSizeX, "width for " + rotation);
            assertEquals(ArsGrid.rotatedDepth(WIDTH, DEPTH, r) * ArsGrid.CELL_SIZE, rotatedSizeZ, "depth for " + rotation);

            for (int cx = 0; cx < WIDTH; cx++) {
                for (int cz = 0; cz < DEPTH; cz++) {
                    BlockPos centre = new BlockPos(cx * ArsGrid.CELL_SIZE + 3, 0, cz * ArsGrid.CELL_SIZE + 3);
                    BlockPos moved = StructureTemplate.transform(centre, Mirror.NONE, rotation, BlockPos.ZERO);
                    ArsCell expected = new ArsCell(
                            Math.floorDiv(moved.getX() - minX, ArsGrid.CELL_SIZE),
                            Math.floorDiv(moved.getZ() - minZ, ArsGrid.CELL_SIZE)
                    );
                    assertEquals(expected, ArsGrid.rotateCell(cx, cz, WIDTH, DEPTH, r),
                            "cell (" + cx + ", " + cz + ") under " + rotation);
                }
            }
        }
    }

    @Test
    void rotateCellClockwise90MatchesDocumentedFormula() {
        // (d-1-cz, cx) for a 3x2 footprint.
        assertEquals(new ArsCell(1, 0), ArsGrid.rotateCell(0, 0, WIDTH, DEPTH, 1));
        assertEquals(new ArsCell(0, 2), ArsGrid.rotateCell(2, 1, WIDTH, DEPTH, 1));
    }

    @Test
    void rotateSideMatchesVanillaRotation() {
        for (Rotation rotation : Rotation.values()) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                assertEquals(rotation.rotate(side), ArsGrid.rotateSide(side, rotation.ordinal()),
                        side + " under " + rotation);
            }
        }
        assertEquals(Direction.EAST, ArsGrid.rotateSide(Direction.NORTH, 1));
    }

    @Test
    void invalidRotationThrows() {
        assertThrows(IllegalArgumentException.class, () -> ArsGrid.rotateCell(0, 0, 1, 1, 4));
        assertThrows(IllegalArgumentException.class, () -> ArsGrid.rotateSide(Direction.NORTH, -1));
    }

    @Test
    void boundsAndReservedCells() {
        assertTrue(ArsGrid.inBounds(0, 0));
        assertTrue(ArsGrid.inBounds(7, 7));
        assertFalse(ArsGrid.inBounds(8, 0));
        assertFalse(ArsGrid.inBounds(0, -1));

        assertTrue(ArsGrid.isReserved(0, 0));
        assertTrue(ArsGrid.isReserved(1, 2));
        assertFalse(ArsGrid.isReserved(2, 1));
        assertFalse(ArsGrid.isReserved(0, 3));
        assertFalse(ArsGrid.isReserved(ArsGrid.ROOT_CELL_X, ArsGrid.ROOT_CELL_Z));
        assertTrue(ArsGrid.isReserved(
                ArsGrid.rootCell().relative(ArsGrid.ROOT_SIDE).x(),
                ArsGrid.rootCell().relative(ArsGrid.ROOT_SIDE).z()));
    }

    @Test
    void cellBlockOrigins() {
        assertEquals(0, ArsGrid.cellMinBlockX(0));
        assertEquals(56, ArsGrid.cellMinBlockX(7));
        assertEquals(16, ArsGrid.cellMinBlockZ(2));
    }
}
