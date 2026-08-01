package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import net.minecraft.util.math.MathHelper;

/**
 * Pure pose math for BOTI synthetic-entity interpolation between sync samples.
 */
public final class BotiEntityMotion {
    /** Matches 1-tick entity flush rate (20 TPS). */
    public static final long EXPECTED_INTERVAL_MS = 50L;

    private BotiEntityMotion() {
    }

    public static float blendFactor(long receiveTimeMs, long nowMs) {
        return blendFactor(receiveTimeMs, nowMs, EXPECTED_INTERVAL_MS);
    }

    public static float blendFactor(long receiveTimeMs, long nowMs, long expectedIntervalMs) {
        if (expectedIntervalMs <= 0L) {
            return 1.0f;
        }
        return MathHelper.clamp((nowMs - receiveTimeMs) / (float) expectedIntervalMs, 0.0f, 1.0f);
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static double lerp(double from, double to, float t) {
        return from + (to - from) * t;
    }

    /** Shortest-path yaw/pitch lerp in degrees. */
    public static float lerpAngleDegrees(float from, float to, float t) {
        return MathHelper.lerpAngleDegrees(t, from, to);
    }

    public static LerpedPose lerpPose(EntityInterpState state, long nowMs) {
        return lerpPose(state, nowMs, EXPECTED_INTERVAL_MS);
    }

    public static LerpedPose lerpPose(EntityInterpState state, long nowMs, long expectedIntervalMs) {
        float t = blendFactor(state.receiveTimeMs(), nowMs, expectedIntervalMs);
        return new LerpedPose(
                lerp(state.fromX(), state.toX(), t),
                lerp(state.fromY(), state.toY(), t),
                lerp(state.fromZ(), state.toZ(), t),
                lerpAngleDegrees(state.fromYaw(), state.toYaw(), t),
                lerp(state.fromPitch(), state.toPitch(), t)
        );
    }

    /**
     * Limb animator speed from one sample step, matching {@code LivingEntity#updateLimbs(float)}.
     */
    public static float limbSpeed(float fromX, float fromZ, float toX, float toZ) {
        float dx = toX - fromX;
        float dz = toZ - fromZ;
        float dist = MathHelper.sqrt(dx * dx + dz * dz);
        return Math.min(dist * 4.0f, 1.0f);
    }

    public record LerpedPose(double x, double y, double z, float yaw, float pitch) {
    }

    /**
     * From/to pose for one synthetic entity between the last two BOTI samples.
     */
    public record EntityInterpState(
            float fromX,
            float fromY,
            float fromZ,
            float fromYaw,
            float fromPitch,
            float toX,
            float toY,
            float toZ,
            float toYaw,
            float toPitch,
            long receiveTimeMs
    ) {
        public static EntityInterpState identity(BotiEntitySample sample, long receiveTimeMs) {
            return identity(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch(), receiveTimeMs);
        }

        public static EntityInterpState identity(
                float x,
                float y,
                float z,
                float yaw,
                float pitch,
                long receiveTimeMs
        ) {
            return new EntityInterpState(x, y, z, yaw, pitch, x, y, z, yaw, pitch, receiveTimeMs);
        }

        public EntityInterpState advanceTo(BotiEntitySample sample, long receiveTimeMs) {
            return advanceTo(sample.relX(), sample.relY(), sample.relZ(), sample.yaw(), sample.pitch(), receiveTimeMs);
        }

        public EntityInterpState advanceTo(
                float x,
                float y,
                float z,
                float yaw,
                float pitch,
                long receiveTimeMs
        ) {
            return new EntityInterpState(
                    toX,
                    toY,
                    toZ,
                    toYaw,
                    toPitch,
                    x,
                    y,
                    z,
                    yaw,
                    pitch,
                    receiveTimeMs
            );
        }
    }
}
