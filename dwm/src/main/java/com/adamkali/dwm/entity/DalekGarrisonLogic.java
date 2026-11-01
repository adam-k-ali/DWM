package com.adamkali.dwm.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * Pure decisions for garrison Dalek jobs: patrol rings, work-spot rotation, the post leash and
 * alarm reach. The goals own movement; this class owns the numbers so they stay unit-testable.
 */
public final class DalekGarrisonLogic {
    /** Beyond this distance from its post a garrison Dalek drops its target and heads home. */
    public static final double LEASH_DISTANCE = 40.0;
    public static final double ARRIVE_DISTANCE = 1.5;
    public static final double FLY_Y_DELTA = 2.5;
    public static final double ALARM_RADIUS = 24.0;
    public static final double LOOKOUT_ALARM_RADIUS = 48.0;
    public static final int PATROL_PAUSE_MIN_TICKS = 40;
    public static final int PATROL_PAUSE_MAX_TICKS = 80;
    public static final int WORK_PAUSE_MIN_TICKS = 60;
    public static final int WORK_PAUSE_MAX_TICKS = 120;
    public static final int DEFAULT_PATROL_RADIUS = 12;
    public static final int PATROL_WAYPOINT_COUNT = 6;
    public static final float SCAN_SWEEP_DEGREES = 35.0F;
    public static final float GUNNER_SWEEP_DEGREES = 70.0F;
    public static final int COURIER_PAUSE_MIN_TICKS = 20;
    public static final int COURIER_PAUSE_MAX_TICKS = 50;

    private DalekGarrisonLogic() {
    }

    /** Evenly spaced ring of {@code count} points around {@code centre}, starting due south and going clockwise. */
    public static List<BlockPos> patrolWaypoints(BlockPos centre, int radius, int count) {
        List<BlockPos> waypoints = new ArrayList<>(Math.max(count, 0));
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count;
            int dx = (int) Math.round(-Math.sin(angle) * radius);
            int dz = (int) Math.round(Math.cos(angle) * radius);
            waypoints.add(centre.offset(dx, 0, dz));
        }
        return waypoints;
    }

    public static int nextWaypoint(int index, int count) {
        return count <= 0 ? 0 : Math.floorMod(index + 1, count);
    }

    /** Next work spot after {@code index}; never repeats the current spot when there is more than one. */
    public static int nextWorkSpot(int index, int count, int roll) {
        if (count <= 1) {
            return 0;
        }
        return Math.floorMod(index + 1 + Math.floorMod(roll, count - 1), count);
    }

    public static boolean shouldDropTarget(double distanceFromPost, double leash) {
        return distanceFromPost > leash;
    }

    public static boolean hasArrived(double distanceSquared, double arriveDistance) {
        return distanceSquared <= arriveDistance * arriveDistance;
    }

    /** Fly when the post is clearly above the Dalek or no ground route reaches it. */
    public static boolean shouldFlyToPost(double postAbove, boolean hasGroundPath) {
        return postAbove > FLY_Y_DELTA || !hasGroundPath;
    }

    public static double alarmRadius(DalekJob job, double baseRadius, double lookoutRadius) {
        return job == DalekJob.LOOKOUT ? lookoutRadius : baseRadius;
    }

    /** Maps a roll in [0, 1) onto [min, max]. */
    public static int pauseTicks(int min, int max, float roll) {
        return min + (int) (roll * (max - min + 1));
    }

    /** Head sweep in degrees for scan tick {@code tick}: oscillates within +/- {@link #SCAN_SWEEP_DEGREES}. */
    public static float scanYawOffset(int tick) {
        return scanYawOffset(tick, SCAN_SWEEP_DEGREES);
    }

    /** Head sweep half-arc for a post-holding job; gunners cover a wider field than sentries. */
    public static float sweepDegrees(DalekJob job) {
        return job == DalekJob.GUNNER ? GUNNER_SWEEP_DEGREES : SCAN_SWEEP_DEGREES;
    }

    /** Head sweep in degrees for scan tick {@code tick} within +/- {@code degrees}. */
    public static float scanYawOffset(int tick, float degrees) {
        return (float) Math.sin(tick * 0.05) * degrees;
    }

    /**
     * Next stop on a courier route of {@code count} stops (post first, then work spots), bouncing
     * off both ends. {@code direction} is +1 or -1; returns {index, direction}.
     */
    public static int[] nextCourierLeg(int index, int direction, int count) {
        if (count <= 1) {
            return new int[] {0, 1};
        }
        int dir = direction >= 0 ? 1 : -1;
        int next = index + dir;
        if (next >= count || next < 0) {
            dir = -dir;
            next = index + dir;
        }
        return new int[] {next, dir};
    }
}
