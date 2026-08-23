package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.model.TardisDoorState;

/**
 * Pure gate logic for whether a doorway portal preview (BOTI or SOTO) should render.
 */
public final class TardisPortalGate {
    private TardisPortalGate() {
    }

    public static boolean shouldShow(float doorSwing) {
        return doorSwing >= TardisDimensions.BOTI_DOOR_SWING_THRESHOLD;
    }

    public static boolean shouldShow(TardisDoorState doorState) {
        if (doorState == null) {
            return false;
        }
        return shouldShow(doorState.doorSwing);
    }
}
