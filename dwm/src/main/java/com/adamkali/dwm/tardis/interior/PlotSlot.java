package com.adamkali.dwm.tardis.interior;

/**
 * One cell of the interior dimension's plot grid. Its anchor is {@code (x, z) * TardisPlotAllocator.PLOT_SPACING}.
 * Allocated per TARDIS and persisted by {@link TardisPlotSlots}.
 */
public record PlotSlot(int x, int z) {
}
