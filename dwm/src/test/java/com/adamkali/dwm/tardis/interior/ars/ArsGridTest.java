package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
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

        assertTrue(ArsGrid.isReserved(3, 3));
        assertTrue(ArsGrid.isReserved(4, 5));
        assertFalse(ArsGrid.isReserved(2, 4));
        assertFalse(ArsGrid.isReserved(5, 4));
        assertFalse(ArsGrid.isReserved(3, 2));
        assertFalse(ArsGrid.isReserved(4, 6));
        // Each root's cell is free and the cell across its face is the console room.
        for (ArsConnector root : ArsGrid.ROOT_CONNECTORS) {
            assertFalse(ArsGrid.isReserved(root.cell().x(), root.cell().z()), root.toString());
            assertTrue(ArsGrid.isReserved(root.facingCell().x(), root.facingCell().z()), root.toString());
        }
        assertEquals(new ArsConnector(new ArsCell(5, 4), Direction.WEST), ArsGrid.EAST_ROOT);
        assertEquals(new ArsConnector(new ArsCell(2, 4), Direction.EAST), ArsGrid.WEST_ROOT);
    }

    @Test
    void consoleRoomSitsInTheMiddleOfItsReservedCells() {
        // Grid-local (0, 0, 0) is the window origin, anchor - (27, 0, 27).
        assertEquals(new BlockPos(TardisPlotAllocator.WINDOW_OFFSET, 0, TardisPlotAllocator.WINDOW_OFFSET), ArsGrid.CONSOLE_ORIGIN);
        assertEquals(new BlockPos(27, 0, 27), ArsGrid.CONSOLE_ORIGIN);
        BlockPos anchor = new BlockPos(64 * 7, 64, 64 * 9);
        assertEquals(anchor, ArsGrid.gridOrigin(anchor).offset(ArsGrid.CONSOLE_ORIGIN));
        // Console room grid-local x 27..37, z 27..43: exactly the reserved cells' span.
        int minX = ArsGrid.CONSOLE_ORIGIN.getX();
        int maxX = minX + FirstDoctorConsoleRoomLayout.SIZE_X - 1;
        int minZ = ArsGrid.CONSOLE_ORIGIN.getZ();
        int maxZ = minZ + FirstDoctorConsoleRoomLayout.SIZE_Z - 1;
        assertEquals(37, maxX);
        assertEquals(43, maxZ);
        assertEquals(ArsGrid.RESERVED_MIN_X, minX / ArsGrid.CELL_SIZE);
        assertEquals(ArsGrid.RESERVED_MAX_X, maxX / ArsGrid.CELL_SIZE);
        assertEquals(ArsGrid.RESERVED_MIN_Z, minZ / ArsGrid.CELL_SIZE);
        assertEquals(ArsGrid.RESERVED_MAX_Z, maxZ / ArsGrid.CELL_SIZE);
        // Free on both sides: three columns west and east, three rows north and two south.
        assertEquals(3, ArsGrid.RESERVED_MIN_X);
        assertEquals(3, ArsGrid.GRID_SIZE - 1 - ArsGrid.RESERVED_MAX_X);
        assertEquals(3, ArsGrid.RESERVED_MIN_Z);
        assertEquals(2, ArsGrid.GRID_SIZE - 1 - ArsGrid.RESERVED_MAX_Z);
    }

    @Test
    void cellBlockOrigins() {
        assertEquals(0, ArsGrid.cellMinBlockX(0));
        assertEquals(56, ArsGrid.cellMinBlockX(7));
        assertEquals(16, ArsGrid.cellMinBlockZ(2));
    }
}
