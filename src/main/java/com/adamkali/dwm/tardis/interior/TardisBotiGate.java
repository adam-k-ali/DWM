package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.model.TardisDoorState;

/**
 * Pure gate logic for whether exterior BOTI (bigger-on-the-inside) preview should render.
 */
public final class TardisBotiGate {
    private TardisBotiGate() {
    }

    public static boolean shouldShow(TardisDoorState doorState) {
        if (doorState == null) {
            return false;
        }
        return doorState.doorSwing >= TardisDimensions.BOTI_DOOR_SWING_THRESHOLD;
    }
}
