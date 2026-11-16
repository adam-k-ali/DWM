package com.adamkali.dwm.tardis.interior;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The plot window: shifted west and north of the anchor so the console room sits in its middle. */
class TardisPlotAllocatorTest {
    private static final int SPACING = TardisPlotAllocator.PLOT_SPACING;
    private static final int OFFSET = TardisPlotAllocator.WINDOW_OFFSET;
    /** Grid slot (10, 20). */
    private static final BlockPos ANCHOR = TardisPlotAllocator.plotOrigin(new UUID(10, 20));

    @Test
    void windowOriginIsAnchorShiftedWestAndNorth() {
        assertEquals(27, OFFSET);
        assertEquals(new BlockPos(640, 64, 1280), ANCHOR);
        assertEquals(new BlockPos(640 - 27, 64, 1280 - 27), TardisPlotAllocator.windowOrigin(ANCHOR));
    }

    @Test
    void windowCoversConsoleRoomAndItsSurroundings() {
        // The console room keeps its place at anchor-local x 0..10, z 0..16.
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
        BlockPos east = TardisPlotAllocator.plotOrigin(new UUID(11, 20));
        BlockPos south = TardisPlotAllocator.plotOrigin(new UUID(10, 21));
        BlockPos diagonal = TardisPlotAllocator.plotOrigin(new UUID(11, 21));
        BlockPos[] anchors = {ANCHOR, east, south, diagonal};
        BlockPos from = TardisPlotAllocator.windowOrigin(ANCHOR);
        // Every block of the 2x2 block of windows belongs to exactly one of them.
        for (int x = 0; x < 2 * SPACING; x++) {
            for (int z = 0; z < 2 * SPACING; z++) {
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
        assertEquals(new BlockPos(0, 64, 0), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(-27, 70, -27)));
        assertEquals(new BlockPos(-64, 64, 0), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(-28, 70, 0)));
        assertEquals(new BlockPos(64, 64, -64), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(37, 0, -28)));
        assertEquals(new BlockPos(0, 64, 0), TardisPlotAllocator.anchorOfWindowAt(new BlockPos(36, 0, 36)));
    }
}
