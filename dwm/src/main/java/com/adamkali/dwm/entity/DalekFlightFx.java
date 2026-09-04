package com.adamkali.dwm.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Pure flight visual helper: exhaust placement, particle counts, and lean angles.
 * No world access — unit-tested in isolation.
 */
public final class DalekFlightFx {
    public static final double EXHAUST_RADIUS = 0.35;
    public static final double EXHAUST_Y = 0.05;
    public static final float BOB_AMPLITUDE = 1.25F;
    public static final float BOB_SPEED = 0.2F;
    public static final float MAX_PITCH_DEGREES = 10.0F;
    public static final float MAX_ROLL_DEGREES = 6.0F;
    public static final int TAKEOFF_BURST_COUNT = 10;

    private static final float REFERENCE_SPEED = 0.4F;
    private static final float PITCH_PER_SPEED = MAX_PITCH_DEGREES / REFERENCE_SPEED;
    private static final float ROLL_PER_SPEED = MAX_ROLL_DEGREES / REFERENCE_SPEED;
    private static final float CLIMB_PITCH_PER_SPEED = 8.0F / REFERENCE_SPEED;

    private DalekFlightFx() {
    }

    public static Vec3 exhaustPos(Vec3 entityPos, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = random.nextDouble() * EXHAUST_RADIUS;
        return entityPos.add(
                Math.cos(angle) * radius,
                EXHAUST_Y,
                Math.sin(angle) * radius
        );
    }

    public static Vec3 exhaustVelocity(Vec3 deltaMovement, RandomSource random) {
        double vx = (random.nextDouble() - 0.5) * 0.04 - deltaMovement.x * 0.15;
        double vy = -0.06 - random.nextDouble() * 0.04;
        double vz = (random.nextDouble() - 0.5) * 0.04 - deltaMovement.z * 0.15;
        return new Vec3(vx, vy, vz);
    }

    public static int cruiseParticleCount(double speed, boolean climbing) {
        int count = 1;
        if (speed > 0.12) {
            count++;
        }
        if (speed > 0.28 || climbing) {
            count++;
        }
        return count;
    }

    public static boolean shouldSpawnClimbCloud(boolean climbing, RandomSource random) {
        return climbing && random.nextFloat() < 0.35F;
    }

    public static int takeoffBurstCount() {
        return TAKEOFF_BURST_COUNT;
    }

    /**
     * World-space velocity into the Dalek's local axes: x = strafe, y = up, z = forward.
     */
    public static Vec3 toLocalVelocity(Vec3 worldVel, float yRotDegrees) {
        float yawRad = yRotDegrees * Mth.DEG_TO_RAD;
        float sin = Mth.sin(yawRad);
        float cos = Mth.cos(yawRad);
        double localX = worldVel.x * cos + worldVel.z * sin;
        double localZ = -worldVel.x * sin + worldVel.z * cos;
        return new Vec3(localX, worldVel.y, localZ);
    }

    public static float leanPitchDegrees(Vec3 localVel, boolean flying) {
        if (!flying) {
            return 0.0F;
        }
        float pitch = (float) (localVel.z * PITCH_PER_SPEED - localVel.y * CLIMB_PITCH_PER_SPEED);
        return Mth.clamp(pitch, -MAX_PITCH_DEGREES, MAX_PITCH_DEGREES);
    }

    public static float leanRollDegrees(Vec3 localVel, boolean flying) {
        if (!flying) {
            return 0.0F;
        }
        float roll = (float) (localVel.x * ROLL_PER_SPEED);
        return Mth.clamp(roll, -MAX_ROLL_DEGREES, MAX_ROLL_DEGREES);
    }

    public static float bobOffset(float ageInTicks, boolean flying) {
        if (!flying) {
            return 0.0F;
        }
        return Mth.sin(ageInTicks * BOB_SPEED) * BOB_AMPLITUDE;
    }
}
