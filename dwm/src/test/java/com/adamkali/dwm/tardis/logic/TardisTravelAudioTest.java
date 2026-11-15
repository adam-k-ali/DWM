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
        assertTrue(TardisTravelAudio.isInsidePlot(ORIGIN.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE), ORIGIN));
        assertTrue(TardisTravelAudio.isInsidePlot(ORIGIN, ORIGIN));
    }

    @Test
    void interiorListenersIncludeArsRoomsAcrossThePlot() {
        int last = TardisPlotAllocator.PLOT_SPACING - 1;
        // Beyond the 11x7x17 console room: east, south, far corner and a tall room's upper floor.
        assertTrue(TardisTravelAudio.isInsidePlot(ORIGIN.offset(30, 2, 10), ORIGIN));
        assertTrue(TardisTravelAudio.isInsidePlot(ORIGIN.offset(4, 1, 40), ORIGIN));
        assertTrue(TardisTravelAudio.isInsidePlot(ORIGIN.offset(last, 0, last), ORIGIN));
        assertTrue(TardisTravelAudio.isInsidePlot(ORIGIN.offset(20, 12, 20), ORIGIN));
    }

    @Test
    void interiorListenersExcludeNeighbouringPlots() {
        assertFalse(TardisTravelAudio.isInsidePlot(ORIGIN.offset(-1, 1, 5), ORIGIN));
        assertFalse(TardisTravelAudio.isInsidePlot(ORIGIN.offset(5, 1, -1), ORIGIN));
        assertFalse(TardisTravelAudio.isInsidePlot(ORIGIN.offset(TardisPlotAllocator.PLOT_SPACING, 1, 5), ORIGIN));
        assertFalse(TardisTravelAudio.isInsidePlot(ORIGIN.offset(5, 1, TardisPlotAllocator.PLOT_SPACING), ORIGIN));
    }
}
