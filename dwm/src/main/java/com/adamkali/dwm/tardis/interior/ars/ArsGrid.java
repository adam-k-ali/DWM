package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Architectural Reconfiguration System grid geometry for one TARDIS interior plot.
 *
 * <p>The grid covers the plot's 64×64-block window ({@link TardisPlotAllocator#windowOrigin}) as an
 * {@value #GRID_SIZE}×{@value #GRID_SIZE} grid of {@value #CELL_SIZE}×{@value #CELL_SIZE}-block cells. Grid-local
 * {@code (0, 0, 0)} is the window origin, so the console room (placed at the plot anchor) starts at grid-local
 * {@link #CONSOLE_ORIGIN} and sits in the middle of the grid. Cell {@code (cx, cz)} covers grid-local blocks
 * {@code x = cx*8 .. cx*8+7}, {@code z = cz*8 .. cz*8+7}. There is a single floor: pieces sit with their floor at
 * grid-local {@code y = 0} (the anchor's height) and are at most {@value #MAX_PIECE_HEIGHT} blocks tall.
 *
 * <p>Rotation is an int {@code 0..3} counting clockwise quarter turns, in vanilla {@code Rotation}
 * order ({@code NONE}, {@code CLOCKWISE_90}, {@code CLOCKWISE_180}, {@code COUNTERCLOCKWISE_90}).
 * {@link #rotateCell} and {@link #rotateSide} match how {@code StructureTemplate} rotates a template
 * placed so that its rotated bounding box starts at the anchor cell's min corner.
 */
public final class ArsGrid {
    /** Blocks per cell edge. */
    public static final int CELL_SIZE = 8;
    /** Cells per plot edge. */
    public static final int GRID_SIZE = 8;
    /** Largest piece footprint edge, in cells. */
    public static final int MAX_PIECE_CELLS = GRID_SIZE;
    /** Grid-local floor height of every piece. */
    public static final int FLOOR_Y = 0;
    /** Tallest piece, in blocks. */
    public static final int MAX_PIECE_HEIGHT = 16;
    public static final int ROTATION_COUNT = 4;

    /**
     * Grid-local position of the plot anchor, where the console room template is placed: the window offset on x
     * and z. Anchor-local positions (console, doors, entrance) convert to grid-local by adding this.
     */
    public static final BlockPos CONSOLE_ORIGIN =
            new BlockPos(TardisPlotAllocator.WINDOW_OFFSET, FLOOR_Y, TardisPlotAllocator.WINDOW_OFFSET);

    /** Console room cells (inclusive); nothing may be placed here. They cover grid-local x 24..39, z 24..47. */
    public static final int RESERVED_MIN_X = 3;
    public static final int RESERVED_MAX_X = 4;
    public static final int RESERVED_MIN_Z = 3;
    public static final int RESERVED_MAX_Z = 5;

    /**
     * The east root: the console room joins the grid at the west face of cell (5, 4), through the east lobby
     * and a doorway in the console room's east wall.
     */
    public static final ArsConnector EAST_ROOT = new ArsConnector(new ArsCell(5, 4), Direction.WEST);
    /**
     * The west root: the console room joins the grid at the east face of cell (2, 4), through the west lobby
     * and a doorway in the console room's west wall.
     */
    public static final ArsConnector WEST_ROOT = new ArsConnector(new ArsCell(2, 4), Direction.EAST);
    /** Every face where the console room joins the grid (east first). A piece connector on one opens it. */
    public static final List<ArsConnector> ROOT_CONNECTORS = List.of(EAST_ROOT, WEST_ROOT);

    /** Connector aperture along a cell face: local offsets {@code 2..5} (inclusive). */
    public static final int APERTURE_MIN_OFFSET = 2;
    public static final int APERTURE_MAX_OFFSET = 5;
    /** Connector aperture height: grid-local {@code y = 1..4} (inclusive). */
    public static final int APERTURE_MIN_Y = 1;
    public static final int APERTURE_MAX_Y = 4;

    private ArsGrid() {
    }

    /** World position of grid-local {@code (0, 0, 0)} for the plot anchored at {@code anchor}: its window origin. */
    public static BlockPos gridOrigin(BlockPos anchor) {
        return TardisPlotAllocator.windowOrigin(anchor);
    }

    public static boolean inBounds(int cellX, int cellZ) {
        return cellX >= 0 && cellX < GRID_SIZE && cellZ >= 0 && cellZ < GRID_SIZE;
    }

    public static boolean isReserved(int cellX, int cellZ) {
        return cellX >= RESERVED_MIN_X && cellX <= RESERVED_MAX_X
                && cellZ >= RESERVED_MIN_Z && cellZ <= RESERVED_MAX_Z;
    }

    public static boolean isValidRotation(int rotation) {
        return rotation >= 0 && rotation < ROTATION_COUNT;
    }

    /** Footprint width (x, in cells) after rotation; odd rotations swap width and depth. */
    public static int rotatedWidth(int width, int depth, int rotation) {
        return (rotation & 1) == 0 ? width : depth;
    }

    /** Footprint depth (z, in cells) after rotation; odd rotations swap width and depth. */
    public static int rotatedDepth(int width, int depth, int rotation) {
        return (rotation & 1) == 0 ? depth : width;
    }

    /**
     * Maps a footprint-local cell of an unrotated {@code width × depth} footprint to its local cell in
     * the rotated footprint (min corner at {@code (0, 0)}).
     *
     * <ul>
     *     <li>0: {@code (cx, cz)}</li>
     *     <li>1 (clockwise 90): {@code (depth-1-cz, cx)}</li>
     *     <li>2 (180): {@code (width-1-cx, depth-1-cz)}</li>
     *     <li>3 (counter-clockwise 90): {@code (cz, width-1-cx)}</li>
     * </ul>
     */
    public static ArsCell rotateCell(int cellX, int cellZ, int width, int depth, int rotation) {
        return switch (rotation) {
            case 0 -> new ArsCell(cellX, cellZ);
            case 1 -> new ArsCell(depth - 1 - cellZ, cellX);
            case 2 -> new ArsCell(width - 1 - cellX, depth - 1 - cellZ);
            case 3 -> new ArsCell(cellZ, width - 1 - cellX);
            default -> throw new IllegalArgumentException("Invalid ARS rotation: " + rotation);
        };
    }

    /** Rotates a horizontal side clockwise {@code rotation} times (north → east → south → west). */
    public static Direction rotateSide(Direction side, int rotation) {
        if (!isValidRotation(rotation)) {
            throw new IllegalArgumentException("Invalid ARS rotation: " + rotation);
        }
        Direction result = side;
        for (int i = 0; i < rotation; i++) {
            result = result.getClockWise();
        }
        return result;
    }

    /** Grid-local block x of a cell's min corner. */
    public static int cellMinBlockX(int cellX) {
        return cellX * CELL_SIZE;
    }

    /** Grid-local block z of a cell's min corner. */
    public static int cellMinBlockZ(int cellZ) {
        return cellZ * CELL_SIZE;
    }
}
