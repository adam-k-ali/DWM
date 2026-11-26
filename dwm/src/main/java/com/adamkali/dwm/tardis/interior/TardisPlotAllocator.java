package com.adamkali.dwm.tardis.interior;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

/**
 * Plot geometry in the shared interior dimension: every TARDIS owns one {@value #PLOT_SPACING}-square plot.
 *
 * <p>Each TARDIS is given a persisted {@link PlotSlot} by {@link TardisPlotSlots}; slots are never shared, so plots
 * never collide. A plot has two origins that must not be mixed:
 * <ul>
 *     <li>The <b>anchor</b> ({@link #plotOrigin}): where the console room is placed. The console room, its door
 *     and entrance positions, the BOTI relative coordinates and the portal stream centre are all anchor-relative.
 *     </li>
 *     <li>The <b>window</b> ({@link #windowOrigin}): the {@value #PLOT_SPACING}-square box (any height) this TARDIS
 *     owns, shifted {@value #WINDOW_OFFSET} blocks west and north of the anchor so the console room sits in its
 *     middle. Every ownership check and clip uses the window. Windows of neighbouring slots tile exactly.</li>
 * </ul>
 *
 * <p>The {@code legacy*} helpers describe the pre-10000 layout (64-block plots hashed from the TARDIS UUID). They are
 * frozen and only used to migrate interiors built before the change.
 */
public final class TardisPlotAllocator {
    public static final int PLOT_SPACING = 10000;
    public static final int PLOT_BASE_Y = 64;
    /** How far the window starts west and north of the anchor, in blocks: 27 + 621 cells of 8. */
    public static final int WINDOW_OFFSET = 4995;
    /** How far the ARS grid origin (cell (0, 0)'s min corner) is west and north of the anchor, in blocks. */
    public static final int GRID_ORIGIN_OFFSET = 27;
    /** Slots run over {@code [-GRID_RADIUS, GRID_RADIUS]} on both axes, keeping every window inside the world border. */
    public static final int GRID_RADIUS = 2999;
    /** Number of slots on one axis. */
    public static final int GRID_WIDTH = 2 * GRID_RADIUS + 1;
    /** Total number of slots ({@link #spiralSlot} indices {@code 0 .. SLOT_COUNT-1}). */
    public static final long SLOT_COUNT = (long) GRID_WIDTH * GRID_WIDTH;

    /** Legacy (pre-10000) plot spacing. Frozen. */
    public static final int LEGACY_PLOT_SPACING = 64;
    /** Legacy window offset west and north of the legacy anchor. Frozen. */
    public static final int LEGACY_WINDOW_OFFSET = 27;
    private static final int LEGACY_GRID_MASK = 0xFFFF;

    private TardisPlotAllocator() {
    }

    /**
     * The anchor of {@code tardisId}'s plot: where its console room is placed. Server-side only: the slot comes from
     * {@link TardisPlotSlots}, which allocates and persists one on first use.
     */
    public static BlockPos plotOrigin(UUID tardisId) {
        if (tardisId == null) {
            throw new IllegalArgumentException("tardisId must not be null");
        }
        return plotOrigin(TardisPlotSlots.slotOf(tardisId));
    }

    /** The anchor of the plot in {@code slot}. */
    public static BlockPos plotOrigin(PlotSlot slot) {
        return new BlockPos(slot.x() * PLOT_SPACING, PLOT_BASE_Y, slot.z() * PLOT_SPACING);
    }

    /** The slot whose anchor is {@code anchor} (any height). */
    public static PlotSlot slotAt(BlockPos anchor) {
        return new PlotSlot(Math.floorDiv(anchor.getX(), PLOT_SPACING), Math.floorDiv(anchor.getZ(), PLOT_SPACING));
    }

    /** Min corner of the window owned by the plot anchored at {@code anchor}: {@code anchor - (4995, 0, 4995)}. */
    public static BlockPos windowOrigin(BlockPos anchor) {
        return anchor.offset(-WINDOW_OFFSET, 0, -WINDOW_OFFSET);
    }

    /**
     * Whether {@code worldPos} lies in the window of the plot anchored at {@code anchor}: x and z in
     * {@code [anchor - 4995, anchor + 5005)}, at any height. Covers the console room and every ARS room.
     */
    public static boolean isInsideWindow(BlockPos worldPos, BlockPos anchor) {
        if (worldPos == null || anchor == null) {
            return false;
        }
        long localX = (long) worldPos.getX() - anchor.getX() + WINDOW_OFFSET;
        long localZ = (long) worldPos.getZ() - anchor.getZ() + WINDOW_OFFSET;
        return localX >= 0 && localX < PLOT_SPACING && localZ >= 0 && localZ < PLOT_SPACING;
    }

    /** The anchor of the slot whose window contains {@code worldPos} (whether or not it is allocated). */
    public static BlockPos anchorOfWindowAt(BlockPos worldPos) {
        int gridX = Math.floorDiv(worldPos.getX() + WINDOW_OFFSET, PLOT_SPACING);
        int gridZ = Math.floorDiv(worldPos.getZ() + WINDOW_OFFSET, PLOT_SPACING);
        return new BlockPos(gridX * PLOT_SPACING, PLOT_BASE_Y, gridZ * PLOT_SPACING);
    }

    /** Whether {@code slot} is inside the allocatable range. */
    public static boolean isValidSlot(PlotSlot slot) {
        return Math.abs(slot.x()) <= GRID_RADIUS && Math.abs(slot.z()) <= GRID_RADIUS;
    }

    /**
     * The {@code index}-th slot of a square spiral out from {@code (0, 0)}: index 0 is the centre, then each ring
     * {@code r} (Chebyshev distance {@code r}) holds {@code 8r} slots, starting just south of its east-north corner and
     * running south along the east edge, west along the south edge, north along the west edge and east along the
     * north edge. Allocation takes the lowest free index, so plots stay close to the world origin.
     */
    public static PlotSlot spiralSlot(long index) {
        if (index < 0 || index >= SLOT_COUNT) {
            throw new IllegalArgumentException("Spiral index out of range: " + index);
        }
        if (index == 0) {
            return new PlotSlot(0, 0);
        }
        // Ring r holds indices [(2r-1)^2, (2r+1)^2).
        int ring = (int) Math.ceil((Math.sqrt(index + 1.0) - 1.0) / 2.0);
        while ((long) (2 * ring - 1) * (2 * ring - 1) > index) {
            ring--;
        }
        while ((long) (2 * ring + 1) * (2 * ring + 1) <= index) {
            ring++;
        }
        long offset = index - (long) (2 * ring - 1) * (2 * ring - 1);
        int side = (int) (offset / (2L * ring));
        int along = (int) (offset % (2L * ring));
        return switch (side) {
            case 0 -> new PlotSlot(ring, -ring + 1 + along);
            case 1 -> new PlotSlot(ring - 1 - along, ring);
            case 2 -> new PlotSlot(-ring, ring - 1 - along);
            default -> new PlotSlot(-ring + 1 + along, -ring);
        };
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

    // ---- legacy (pre-10000) layout, frozen for migration -------------------------------------------------

    /** The anchor {@code tardisId}'s console room had before plots grew to 10000 blocks. */
    public static BlockPos legacyPlotOrigin(UUID tardisId) {
        if (tardisId == null) {
            throw new IllegalArgumentException("tardisId must not be null");
        }
        int gridX = (int) (tardisId.getMostSignificantBits() & LEGACY_GRID_MASK);
        int gridZ = (int) (tardisId.getLeastSignificantBits() & LEGACY_GRID_MASK);
        return new BlockPos(gridX * LEGACY_PLOT_SPACING, PLOT_BASE_Y, gridZ * LEGACY_PLOT_SPACING);
    }

    /** Min corner of the legacy 64-square window of the legacy plot anchored at {@code legacyAnchor}. */
    public static BlockPos legacyWindowOrigin(BlockPos legacyAnchor) {
        return legacyAnchor.offset(-LEGACY_WINDOW_OFFSET, 0, -LEGACY_WINDOW_OFFSET);
    }

    /** Whether {@code worldPos} is in the legacy window of the legacy plot anchored at {@code legacyAnchor}. */
    public static boolean isInsideLegacyWindow(BlockPos worldPos, BlockPos legacyAnchor) {
        if (worldPos == null || legacyAnchor == null) {
            return false;
        }
        int localX = worldPos.getX() - legacyAnchor.getX() + LEGACY_WINDOW_OFFSET;
        int localZ = worldPos.getZ() - legacyAnchor.getZ() + LEGACY_WINDOW_OFFSET;
        return localX >= 0 && localX < LEGACY_PLOT_SPACING && localZ >= 0 && localZ < LEGACY_PLOT_SPACING;
    }

    /** How far a migrated interior moves: {@code plotOrigin(id) - legacyPlotOrigin(id)}. */
    public static Vec3i migrationDelta(UUID tardisId) {
        return migrationDelta(legacyPlotOrigin(tardisId), plotOrigin(tardisId));
    }

    /** {@code newAnchor - legacyAnchor}. */
    public static Vec3i migrationDelta(BlockPos legacyAnchor, BlockPos newAnchor) {
        return new Vec3i(
                newAnchor.getX() - legacyAnchor.getX(),
                newAnchor.getY() - legacyAnchor.getY(),
                newAnchor.getZ() - legacyAnchor.getZ());
    }
}
