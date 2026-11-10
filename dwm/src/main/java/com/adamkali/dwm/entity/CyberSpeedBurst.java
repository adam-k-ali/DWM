package com.adamkali.dwm.entity;

/**
 * Weapons-grade Cyberman super-speed: when its target is between {@link #MIN_DISTANCE} and
 * {@link #MAX_DISTANCE} blocks away it dashes, moving {@link #SPEED_MULTIPLIER} times faster for
 * {@link #BURST_TICKS}, then cannot dash again for {@link #COOLDOWN_TICKS}.
 */
public final class CyberSpeedBurst {
    public static final double MIN_DISTANCE = 6.0;
    public static final double MAX_DISTANCE = 24.0;
    /** Dash length in ticks (1.5 seconds). */
    public static final int BURST_TICKS = 30;
    /** Ticks after a dash ends before another may start (6 seconds). */
    public static final int COOLDOWN_TICKS = 120;
    /** Movement-speed bonus while dashing, as an {@code add_multiplied_base} modifier amount. */
    public static final double SPEED_MULTIPLIER = 1.5;

    private CyberSpeedBurst() {
    }

    /**
     * @param hasTarget whether the Cyberman has a live target
     * @param distanceToTarget distance to that target in blocks
     * @param cooldownRemaining ticks left before another dash is allowed
     */
    public static boolean shouldBurst(boolean hasTarget, double distanceToTarget, int cooldownRemaining) {
        return hasTarget
                && cooldownRemaining <= 0
                && distanceToTarget >= MIN_DISTANCE
                && distanceToTarget <= MAX_DISTANCE;
    }
}
