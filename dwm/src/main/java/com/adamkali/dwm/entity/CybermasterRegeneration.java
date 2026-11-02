package com.adamkali.dwm.entity;

/**
 * Cybermaster regeneration: built from Time Lords, a Cybermaster survives its first killing blow once,
 * returning to full health. Damage that bypasses invulnerability ({@code /kill}, the void) is never
 * survived.
 */
public final class CybermasterRegeneration {
    /** Resistance granted while the regeneration settles, in ticks (3 seconds). */
    public static final int RESISTANCE_TICKS = 60;
    /** Resistance amplifier (2 = Resistance III). */
    public static final int RESISTANCE_AMPLIFIER = 2;
    /** Regeneration-energy particles spawned when it regenerates. */
    public static final int PARTICLE_COUNT = 40;

    private CybermasterRegeneration() {
    }

    /**
     * @param dead whether the hit left the Cybermaster at zero health
     * @param alreadyRegenerated whether this Cybermaster has already used its regeneration
     * @param bypassesInvulnerability whether the killing damage is tagged {@code bypasses_invulnerability}
     */
    public static boolean shouldRegenerate(boolean dead, boolean alreadyRegenerated, boolean bypassesInvulnerability) {
        return dead && !alreadyRegenerated && !bypassesInvulnerability;
    }
}
