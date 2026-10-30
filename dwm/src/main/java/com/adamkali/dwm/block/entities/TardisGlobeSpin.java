package com.adamkali.dwm.block.entities;

/**
 * Transient spin-up state for the TARDIS globe: a right-click flicks the sphere through
 * {@link #TURNS_DEGREES} with an ease-out over {@link #DURATION_TICKS}. Pure game-time maths so
 * both sides agree and it can be unit tested; never persisted.
 */
public final class TardisGlobeSpin {
    public static final int DURATION_TICKS = 60;
    public static final float TURNS_DEGREES = 1080.0F;

    /** Ticks over which {@link #energy} ramps up before it starts to decay. */
    private static final float ATTACK = 1.0F / 8.0F;

    private float baseDegrees;
    private long startTick = Long.MIN_VALUE;

    /**
     * Starts a new spin at {@code gameTime}. The current angle is folded into the base, so a
     * re-click mid-spin carries on from where the sphere is instead of snapping back.
     */
    public void start(long gameTime) {
        baseDegrees = degrees(gameTime, 0.0F) % 360.0F;
        startTick = gameTime;
    }

    /** Extra sphere rotation in degrees (the base, plus the eased turns while spinning). */
    public float degrees(long gameTime, float partialTick) {
        float progress = progress(gameTime, partialTick);
        if (progress <= 0.0F) {
            return baseDegrees;
        }
        return baseDegrees + TURNS_DEGREES * easeOutCubic(progress);
    }

    /** 0-1 interaction envelope: a fast attack, then a slow decay back to 0 by the end. */
    public float energy(long gameTime, float partialTick) {
        float progress = progress(gameTime, partialTick);
        if (progress <= 0.0F || progress >= 1.0F) {
            return 0.0F;
        }
        float decay = 1.0F - progress;
        return Math.min(1.0F, progress / ATTACK) * decay * decay;
    }

    public boolean isSpinning(long gameTime, float partialTick) {
        float progress = progress(gameTime, partialTick);
        return progress > 0.0F && progress < 1.0F;
    }

    /** 0 before any spin, then 0-1 through the spin, held at 1 once it ends. */
    private float progress(long gameTime, float partialTick) {
        if (startTick == Long.MIN_VALUE) {
            return 0.0F;
        }
        float elapsed = (float) (gameTime - startTick) + partialTick;
        if (elapsed <= 0.0F) {
            return 0.0F;
        }
        return Math.min(1.0F, elapsed / DURATION_TICKS);
    }

    static float easeOutCubic(float t) {
        float inverse = 1.0F - t;
        return 1.0F - inverse * inverse * inverse;
    }
}
