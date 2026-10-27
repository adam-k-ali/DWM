package com.adamkali.dwm.entity;

/**
 * Pure decision helper for the Dalek's "Exterminate!" shout when it locks onto a target.
 * No world access — unit-tested in isolation.
 */
public final class DalekVoice {
    /** Minimum ticks between two shouts from the same Dalek (10 seconds). */
    public static final int EXTERMINATE_COOLDOWN_TICKS = 200;

    private DalekVoice() {
    }

    /**
     * Whether a target change should make the Dalek shout: only on a fresh lock (no target
     * before, a target now) and once the cooldown since its last shout has elapsed.
     */
    public static boolean shouldExterminate(boolean hadTarget, boolean hasTarget, int ticksSinceLastShout) {
        return !hadTarget && hasTarget && ticksSinceLastShout >= EXTERMINATE_COOLDOWN_TICKS;
    }
}
