package com.adamkali.dwm.tardis.interior;

import java.util.UUID;
import net.minecraft.core.BlockPos;

/**
 * Assigns each TARDIS a deterministic, non-overlapping plot origin in the shared interior dimension.
 */
public final class TardisPlotAllocator {
    public static final int PLOT_SPACING = 64;
    public static final int PLOT_BASE_Y = 64;
    private static final int GRID_MASK = 0xFFFF;

    private TardisPlotAllocator() {
    }

    public static BlockPos plotOrigin(UUID tardisId) {
        if (tardisId == null) {
            throw new IllegalArgumentException("tardisId must not be null");
        }
        int gridX = (int) (tardisId.getMostSignificantBits() & GRID_MASK);
        int gridZ = (int) (tardisId.getLeastSignificantBits() & GRID_MASK);
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
