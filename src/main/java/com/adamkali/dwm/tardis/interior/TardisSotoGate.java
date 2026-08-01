package com.adamkali.dwm.tardis.interior;

/**
 * Pure gate logic for whether interior SOTO (smaller-on-the-outside) preview should render.
 */
public final class TardisSotoGate {
    private TardisSotoGate() {
    }

    /**
     * SOTO appears once the interior door has cracked open enough to see through —
     * same threshold as exterior BOTI.
     */
    public static boolean shouldShow(float doorSwing) {
        return doorSwing >= TardisDimensions.BOTI_DOOR_SWING_THRESHOLD;
    }
}
