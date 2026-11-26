package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisTravelAudioTest {
    private static final BlockPos ORIGIN = new BlockPos(640, TardisPlotAllocator.PLOT_BASE_Y, -1280);

    @Test
    void interiorListenersIncludeConsoleRoom() {
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE), ORIGIN));
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN, ORIGIN));
    }

    @Test
    void interiorListenersIncludeArsRoomsAcrossTheWindow() {
        int offset = TardisPlotAllocator.WINDOW_OFFSET;
        int last = TardisPlotAllocator.PLOT_SPACING - offset - 1;
        // Beyond the 11x7x17 console room: east, west, south, both far corners and a tall room's upper floor.
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(30, 2, 10), ORIGIN));
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(-20, 2, 10), ORIGIN));
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(4, 1, 30), ORIGIN));
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(last, 0, last), ORIGIN));
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(-offset, 0, -offset), ORIGIN));
        assertTrue(TardisTravelAudio.isInsideWindow(ORIGIN.offset(20, 12, -20), ORIGIN));
    }

    @Test
    void interiorListenersExcludeNeighbouringWindows() {
        int offset = TardisPlotAllocator.WINDOW_OFFSET;
        int span = TardisPlotAllocator.PLOT_SPACING - offset;
        assertFalse(TardisTravelAudio.isInsideWindow(ORIGIN.offset(-offset - 1, 1, 5), ORIGIN));
        assertFalse(TardisTravelAudio.isInsideWindow(ORIGIN.offset(5, 1, -offset - 1), ORIGIN));
        assertFalse(TardisTravelAudio.isInsideWindow(ORIGIN.offset(span, 1, 5), ORIGIN));
        assertFalse(TardisTravelAudio.isInsideWindow(ORIGIN.offset(5, 1, span), ORIGIN));
    }
}
