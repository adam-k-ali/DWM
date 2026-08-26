package com.adamkali.dwm.render;

import net.minecraft.util.Mth;

/**
 * Pure timing and close-decision rules for the sonic field-mode carousel.
 */
public final class SonicCarouselMotion {
    public static final long DURATION_MS = 160L;

    private SonicCarouselMotion() {
    }

    public static float progress(long startedAtMs, long nowMs) {
        return Mth.clamp((nowMs - startedAtMs) / (float) DURATION_MS, 0.0f, 1.0f);
    }

    public static float easedProgress(long startedAtMs, long nowMs) {
        float t = progress(startedAtMs, nowMs);
        return t * t * (3.0f - 2.0f * t);
    }

    public static float value(float from, float target, long startedAtMs, long nowMs) {
        return Mth.lerp(easedProgress(startedAtMs, nowMs), from, target);
    }

    /**
     * Samples an in-flight transition before assigning a new target, preventing jumps on rapid input.
     */
    public static Transition retarget(
            float from,
            float previousTarget,
            long previousStartedAtMs,
            float newTarget,
            long nowMs
    ) {
        return new Transition(value(from, previousTarget, previousStartedAtMs, nowMs), newTarget, nowMs);
    }

    /**
     * A restrained settle pulse used only by the incoming selected slot.
     */
    public static float selectedScaleBoost(long startedAtMs, long nowMs) {
        float t = progress(startedAtMs, nowMs);
        if (t < 0.65f || t >= 1.0f) {
            return 0.0f;
        }
        float settle = (t - 0.65f) / 0.35f;
        return 0.045f * Mth.sin(settle * Mth.PI);
    }

    public static int selectionPhase(long startedAtMs, long nowMs) {
        return Math.min(3, (int) (progress(startedAtMs, nowMs) * 4.0f));
    }

    public static CloseDecision closeDecision(CloseReason reason, boolean unlocked, boolean changed) {
        if (reason != CloseReason.RELEASE) {
            return CloseDecision.CANCEL;
        }
        if (!unlocked) {
            return CloseDecision.REJECT_LOCKED;
        }
        return changed ? CloseDecision.COMMIT : CloseDecision.CLOSE_UNCHANGED;
    }

    public enum CloseReason {
        RELEASE,
        ESCAPE,
        FORCED
    }

    public enum CloseDecision {
        COMMIT,
        CLOSE_UNCHANGED,
        REJECT_LOCKED,
        CANCEL
    }

    public record Transition(float from, float target, long startedAtMs) {
    }
}
