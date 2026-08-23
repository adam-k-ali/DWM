package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.model.TardisDoorState;

/**
 * Pure gate logic for whether a player may enter a TARDIS interior from the exterior.
 */
public final class TardisEntryGate {
    private TardisEntryGate() {
    }

    public static boolean canEnter(TardisDoorState doorState) {
        if (doorState == null) {
            return false;
        }
        return doorState.isOpen && doorState.doorSwing >= TardisDimensions.ENTRY_DOOR_SWING_THRESHOLD;
    }
}
