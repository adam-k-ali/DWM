package com.adamkali.dwm.entity;

import net.minecraft.util.Mth;

/**
 * Pure job-animation helper: which pose a Dalek's job drives and how fast it blends in.
 * No world access — unit-tested in isolation.
 */
public final class DalekJobFx {
    /** Blend change per tick; about a quarter of a second from idle to full pose. */
    public static final float BLEND_PER_TICK = 0.1F;

    private DalekJobFx() {
    }

    /** 1 while an on-duty sentry, lookout or patroller should sweep its eyestalk, else 0. */
    public static float scanTarget(DalekJob job, boolean onDuty) {
        boolean scans = job == DalekJob.SENTRY || job == DalekJob.LOOKOUT || job == DalekJob.PATROL;
        return onDuty && scans ? 1.0F : 0.0F;
    }

    /** 1 while an on-duty technician should work its plunger, else 0. */
    public static float workTarget(DalekJob job, boolean onDuty) {
        return onDuty && job == DalekJob.TECHNICIAN ? 1.0F : 0.0F;
    }

    /** Moves {@code current} toward {@code target} by at most {@link #BLEND_PER_TICK}. */
    public static float ease(float current, float target) {
        return Mth.clamp(target, current - BLEND_PER_TICK, current + BLEND_PER_TICK);
    }
}
