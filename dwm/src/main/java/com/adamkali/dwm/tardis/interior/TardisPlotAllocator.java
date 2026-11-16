package com.adamkali.dwm.tardis.interior;

import java.util.UUID;
import net.minecraft.core.BlockPos;

/**
 * Assigns each TARDIS a deterministic, non-overlapping plot in the shared interior dimension.
 *
 * <p>A plot has two origins that must not be mixed:
 * <ul>
 *     <li>The <b>anchor</b> ({@link #plotOrigin}): where the console room is placed. The console room, its door
 *     and entrance positions, the BOTI relative coordinates and the portal stream centre are all anchor-relative.
 *     </li>
 *     <li>The <b>window</b> ({@link #windowOrigin}): the {@value #PLOT_SPACING}-square box (any height) this TARDIS
 *     owns, shifted {@value #WINDOW_OFFSET} blocks west and north of the anchor so the console room sits in its
 *     middle. Every ownership check and clip uses the window. Windows of neighbouring plots tile exactly.</li>
 * </ul>
 */
public final class TardisPlotAllocator {
    public static final int PLOT_SPACING = 64;
    public static final int PLOT_BASE_Y = 64;
    /** How far the window starts west and north of the anchor, in blocks. */
    public static final int WINDOW_OFFSET = 27;
    private static final int GRID_MASK = 0xFFFF;

    private TardisPlotAllocator() {
    }

    /** The anchor of {@code tardisId}'s plot: where its console room is placed. */
    public static BlockPos plotOrigin(UUID tardisId) {
        if (tardisId == null) {
            throw new IllegalArgumentException("tardisId must not be null");
        }
        int gridX = (int) (tardisId.getMostSignificantBits() & GRID_MASK);
        int gridZ = (int) (tardisId.getLeastSignificantBits() & GRID_MASK);
        return new BlockPos(gridX * PLOT_SPACING, PLOT_BASE_Y, gridZ * PLOT_SPACING);
    }

    /** Min corner of the window owned by the plot anchored at {@code anchor}: {@code anchor - (27, 0, 27)}. */
    public static BlockPos windowOrigin(BlockPos anchor) {
        return anchor.offset(-WINDOW_OFFSET, 0, -WINDOW_OFFSET);
    }

    /**
     * Whether {@code worldPos} lies in the window of the plot anchored at {@code anchor}: x and z in
     * {@code [anchor - 27, anchor + 37)}, at any height. Covers the console room and every ARS room.
     */
    public static boolean isInsideWindow(BlockPos worldPos, BlockPos anchor) {
        if (worldPos == null || anchor == null) {
            return false;
        }
        int localX = worldPos.getX() - anchor.getX() + WINDOW_OFFSET;
        int localZ = worldPos.getZ() - anchor.getZ() + WINDOW_OFFSET;
        return localX >= 0 && localX < PLOT_SPACING && localZ >= 0 && localZ < PLOT_SPACING;
    }

    /** The anchor of the plot grid slot whose window contains {@code worldPos} (whether or not it is allocated). */
    public static BlockPos anchorOfWindowAt(BlockPos worldPos) {
        int gridX = Math.floorDiv(worldPos.getX() + WINDOW_OFFSET, PLOT_SPACING);
        int gridZ = Math.floorDiv(worldPos.getZ() + WINDOW_OFFSET, PLOT_SPACING);
        return new BlockPos(gridX * PLOT_SPACING, PLOT_BASE_Y, gridZ * PLOT_SPACING);
    }

    /**
     * Distinct allocated plots are separated by {@link #PLOT_SPACING} on at least one horizontal axis,
     * which is enough for structures no larger than that spacing.
     */
    public static boolean plotsAreSeparated(BlockPos a, BlockPos b, int maxStructureExtent) {
        if (a.equals(b)) {
            return true;
        }
        if (maxStructureExtent > PLOT_SPACING) {
            return false;
        }
        int dx = Math.abs(a.getX() - b.getX());
        int dz = Math.abs(a.getZ() - b.getZ());
        return dx >= PLOT_SPACING || dz >= PLOT_SPACING;
    }
}
