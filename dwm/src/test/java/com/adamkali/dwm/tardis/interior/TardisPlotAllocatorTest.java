package com.adamkali.dwm.tardis.interior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plot geometry: 10000-block windows shifted west and north of the anchor so the console room sits in the middle. */
class TardisPlotAllocatorTest {
    private static final int SPACING = TardisPlotAllocator.PLOT_SPACING;
    private static final int OFFSET = TardisPlotAllocator.WINDOW_OFFSET;
    /** Vanilla's world border limit (blocks beyond it are unbuildable). */
    private static final int WORLD_LIMIT = 29_999_984;
    private static final BlockPos ANCHOR = TardisPlotAllocator.plotOrigin(new PlotSlot(10, -20));

    @Test
    void windowOriginIsAnchorShiftedWestAndNorth() {
        assertEquals(10000, SPACING);
        assertEquals(4995, OFFSET);
        assertEquals(27, TardisPlotAllocator.GRID_ORIGIN_OFFSET);
        assertEquals(new BlockPos(100_000, 64, -200_000), ANCHOR);
        assertEquals(new BlockPos(100_000 - 4995, 64, -200_000 - 4995), TardisPlotAllocator.windowOrigin(ANCHOR));
        // The console room (anchor-local x 0..10, z 0..16) is in the middle of the window.
        int westRoom = OFFSET;
        int eastRoom = SPACING - OFFSET - FirstDoctorConsoleRoomLayout.SIZE_X;
        assertTrue(Math.abs(westRoom - eastRoom) <= 1, westRoom + " vs " + eastRoom);
    }

    @Test
    void windowCoversConsoleRoomAndItsSurroundings() {
        assertTrue(TardisPlotAllocator.isInsideWindow(ANCHOR, ANCHOR));
        assertTrue(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(FirstDoctorConsoleRoomLayout.SIZE_X - 1, 3,
                FirstDoctorConsoleRoomLayout.SIZE_Z - 1), ANCHOR));
        assertTrue(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(-OFFSET, -40, -OFFSET), ANCHOR));
        int last = SPACING - OFFSET - 1;
        assertTrue(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(last, 90, last), ANCHOR));
        assertTrue(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(-1, 1, 5), ANCHOR));

        assertFalse(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(-OFFSET - 1, 1, 5), ANCHOR));
        assertFalse(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(5, 1, -OFFSET - 1), ANCHOR));
        assertFalse(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(last + 1, 1, 5), ANCHOR));
        assertFalse(TardisPlotAllocator.isInsideWindow(ANCHOR.offset(5, 1, last + 1), ANCHOR));
        assertFalse(TardisPlotAllocator.isInsideWindow(null, ANCHOR));
        assertFalse(TardisPlotAllocator.isInsideWindow(ANCHOR, null));
    }

    @Test
    void adjacentWindowsTileWithNoGapOrOverlapOnBothAxes() {
        BlockPos east = TardisPlotAllocator.plotOrigin(new PlotSlot(11, -20));
        BlockPos south = TardisPlotAllocator.plotOrigin(new PlotSlot(10, -19));
        BlockPos diagonal = TardisPlotAllocator.plotOrigin(new PlotSlot(11, -19));
        BlockPos[] anchors = {ANCHOR, east, south, diagonal};
        BlockPos from = TardisPlotAllocator.windowOrigin(ANCHOR);
        // Every sampled block of the 2x2 block of windows (both edges of every boundary, plus a stride through the
        // interiors) belongs to exactly one window, and anchorOfWindowAt finds it.
        Set<Integer> samples = new HashSet<>();
        for (int i = 0; i < 2 * SPACING; i += 97) {
            samples.add(i);
        }
        for (int edge : new int[]{0, SPACING - 1, SPACING, 2 * SPACING - 1}) {
            samples.add(edge);
        }
        for (int x : samples) {
            for (int z : samples) {
                BlockPos pos = from.offset(x, 0, z);
                int owners = 0;
                for (BlockPos anchor : anchors) {
                    if (TardisPlotAllocator.isInsideWindow(pos, anchor)) {
                        owners++;
                        assertEquals(anchor, TardisPlotAllocator.anchorOfWindowAt(pos), pos.toString());
                    }
                }
                assertEquals(1, owners, "owners of " + pos);
            }
        }
        // The boundary on each axis: the last block of one window touches the first of the next.
        assertTrue(TardisPlotAllocator.isInsideWindow(east.offset(-OFFSET - 1, 0, 0), ANCHOR));
        assertTrue(TardisPlotAllocator.isInsideWindow(east.offset(-OFFSET, 0, 0), east));
        assertTrue(TardisPlotAllocator.isInsideWindow(south.offset(0, 0, -OFFSET - 1), ANCHOR));
        assertTrue(TardisPlotAllocator.isInsideWindow(south.offset(0, 0, -OFFSET), south));
    }

    @Test
    void anchorOfWindowAtHandlesNegativeCoordinates() {
        assertEquals(new BlockPos(0, 64, 0), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(-4995, 70, -4995)));
        assertEquals(new BlockPos(-SPACING, 64, 0), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(-4996, 70, 0)));
        assertEquals(new BlockPos(SPACING, 64, -SPACING),
                TardisPlotAllocator.anchorOfWindowAt(new BlockPos(5005, 0, -4996)));
        assertEquals(new BlockPos(0, 64, 0), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(5004, 0, 5004)));
    }

    @Test
    void slotAtInvertsPlotOrigin() {
        for (PlotSlot slot : new PlotSlot[]{new PlotSlot(0, 0), new PlotSlot(-1, 3), new PlotSlot(2999, -2999)}) {
            assertEquals(slot, TardisPlotAllocator.slotAt(TardisPlotAllocator.plotOrigin(slot)));
            assertEquals(slot, TardisPlotAllocator.slotAt(
                    TardisPlotAllocator.anchorOfWindowAt(TardisPlotAllocator.plotOrigin(slot).offset(-4000, 0, 4000))));
        }
    }

    @Test
    void everySlotWindowStaysInsideTheWorldBorder() {
        int radius = TardisPlotAllocator.GRID_RADIUS;
        BlockPos max = TardisPlotAllocator.plotOrigin(new PlotSlot(radius, radius));
        BlockPos min = TardisPlotAllocator.plotOrigin(new PlotSlot(-radius, -radius));
        BlockPos minWindow = TardisPlotAllocator.windowOrigin(min);
        int maxEdge = TardisPlotAllocator.windowOrigin(max).getX() + SPACING - 1;
        assertTrue(maxEdge < WORLD_LIMIT, "east edge " + maxEdge);
        assertTrue(minWindow.getX() > -WORLD_LIMIT, "west edge " + minWindow.getX());
        assertTrue(TardisPlotAllocator.isValidSlot(new PlotSlot(radius, -radius)));
        assertFalse(TardisPlotAllocator.isValidSlot(new PlotSlot(radius + 1, 0)));
        assertFalse(TardisPlotAllocator.isValidSlot(new PlotSlot(0, -radius - 1)));
        assertEquals((long) TardisPlotAllocator.GRID_WIDTH * TardisPlotAllocator.GRID_WIDTH, TardisPlotAllocator.SLOT_COUNT);
    }

    @Test
    void spiralStartsAtTheCentreAndRingsOutward() {
        assertEquals(new PlotSlot(0, 0), TardisPlotAllocator.spiralSlot(0));
        assertEquals(new PlotSlot(1, 0), TardisPlotAllocator.spiralSlot(1));
        assertEquals(new PlotSlot(1, 1), TardisPlotAllocator.spiralSlot(2));
        assertEquals(new PlotSlot(0, 1), TardisPlotAllocator.spiralSlot(3));
        assertEquals(new PlotSlot(1, -1), TardisPlotAllocator.spiralSlot(8));
        assertEquals(new PlotSlot(2, -1), TardisPlotAllocator.spiralSlot(9));
        // Indices fill each ring exactly before moving to the next.
        Set<PlotSlot> seen = new HashSet<>();
        int n = 121;
        for (int i = 0; i < n; i++) {
            PlotSlot slot = TardisPlotAllocator.spiralSlot(i);
            assertTrue(seen.add(slot), "duplicate " + slot + " at " + i);
            assertTrue(Math.max(Math.abs(slot.x()), Math.abs(slot.z())) <= 5, slot.toString());
        }
        assertEquals(n, seen.size());
        // The very last index is a corner of the outermost ring; past it is refused.
        PlotSlot last = TardisPlotAllocator.spiralSlot(TardisPlotAllocator.SLOT_COUNT - 1);
        assertEquals(TardisPlotAllocator.GRID_RADIUS, Math.max(Math.abs(last.x()), Math.abs(last.z())));
        assertTrue(TardisPlotAllocator.isValidSlot(last));
        assertThrows(IllegalArgumentException.class, () -> TardisPlotAllocator.spiralSlot(TardisPlotAllocator.SLOT_COUNT));
        assertThrows(IllegalArgumentException.class, () -> TardisPlotAllocator.spiralSlot(-1));
    }

    @Test
    void spiralRingsAreCompleteAtLargeIndices() {
        long start = (long) (2 * 1000 - 1) * (2 * 1000 - 1);
        Set<PlotSlot> ring = new HashSet<>();
        for (long i = start; i < start + 8 * 1000; i++) {
            PlotSlot slot = TardisPlotAllocator.spiralSlot(i);
            assertEquals(1000, Math.max(Math.abs(slot.x()), Math.abs(slot.z())), "index " + i);
            ring.add(slot);
        }
        assertEquals(8000, ring.size());
    }

    @Test
    void legacyLayoutIsFrozen() {
        // Exact pre-10000 values: grid slot (10, 20) of 64-block plots, window anchor - 27 .. anchor + 36.
        BlockPos legacy = TardisPlotAllocator.legacyPlotOrigin(new UUID(10, 20));
        assertEquals(new BlockPos(640, 64, 1280), legacy);
        assertEquals(new BlockPos(613, 64, 1253), TardisPlotAllocator.legacyWindowOrigin(legacy));
        assertEquals(new BlockPos(0xFFFF * 64, 64, 0), TardisPlotAllocator.legacyPlotOrigin(new UUID(-1, 0x10000)));
        assertTrue(TardisPlotAllocator.isInsideLegacyWindow(legacy.offset(-27, 5, -27), legacy));
        assertTrue(TardisPlotAllocator.isInsideLegacyWindow(legacy.offset(36, -5, 36), legacy));
        assertFalse(TardisPlotAllocator.isInsideLegacyWindow(legacy.offset(-28, 0, 0), legacy));
        assertFalse(TardisPlotAllocator.isInsideLegacyWindow(legacy.offset(0, 0, 37), legacy));
        assertFalse(TardisPlotAllocator.isInsideLegacyWindow(null, legacy));
    }

    @Test
    void legacyWindowMapsOntoTheSameAnchorRelativeRegionOfTheNewPlot() {
        BlockPos legacy = TardisPlotAllocator.legacyPlotOrigin(new UUID(10, 20));
        BlockPos fresh = TardisPlotAllocator.plotOrigin(new PlotSlot(-3, 2));
        Vec3i delta = TardisPlotAllocator.migrationDelta(legacy, fresh);
        assertEquals(new Vec3i(-30_000 - 640, 0, 20_000 - 1280), delta);
        for (int x = -27; x <= 36; x += 63) {
            for (int z = -27; z <= 36; z += 63) {
                BlockPos moved = legacy.offset(x, 3, z).offset(delta);
                assertEquals(fresh.offset(x, 3, z), moved);
                assertTrue(TardisPlotAllocator.isInsideWindow(moved, fresh));
            }
        }
    }
}
