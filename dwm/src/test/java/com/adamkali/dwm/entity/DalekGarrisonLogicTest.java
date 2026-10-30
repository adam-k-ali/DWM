package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekGarrisonLogicTest {
    private static final BlockPos CENTRE = new BlockPos(100, 64, -40);

    @Test
    void patrolRingStartsDueSouthAndHasTheRequestedSize() {
        List<BlockPos> ring = DalekGarrisonLogic.patrolWaypoints(CENTRE, 12, 6);
        assertEquals(6, ring.size());
        assertEquals(CENTRE.offset(0, 0, 12), ring.get(0));
        assertEquals(6, new HashSet<>(ring).size());
    }

    @Test
    void patrolRingPointsSitOnTheRadiusAtTheCentreHeight() {
        for (BlockPos point : DalekGarrisonLogic.patrolWaypoints(CENTRE, 12, 6)) {
            assertEquals(CENTRE.getY(), point.getY());
            double dist = Math.sqrt(point.distSqr(CENTRE));
            assertEquals(12.0, dist, 1.0);
        }
    }

    @Test
    void patrolRingIsDeterministicAndEmptyForNoPoints() {
        assertEquals(
                DalekGarrisonLogic.patrolWaypoints(CENTRE, 8, 4),
                DalekGarrisonLogic.patrolWaypoints(CENTRE, 8, 4)
        );
        assertTrue(DalekGarrisonLogic.patrolWaypoints(CENTRE, 8, 0).isEmpty());
    }

    @Test
    void waypointIndexWrapsAround() {
        assertEquals(1, DalekGarrisonLogic.nextWaypoint(0, 6));
        assertEquals(0, DalekGarrisonLogic.nextWaypoint(5, 6));
        assertEquals(0, DalekGarrisonLogic.nextWaypoint(3, 0));
    }

    @Test
    void workSpotNeverRepeatsTheCurrentOneWhenThereAreSeveral() {
        for (int count = 2; count <= 5; count++) {
            for (int index = 0; index < count; index++) {
                for (int roll = 0; roll < 10; roll++) {
                    int next = DalekGarrisonLogic.nextWorkSpot(index, count, roll);
                    assertTrue(next >= 0 && next < count);
                    assertTrue(next != index, count + "/" + index + "/" + roll);
                }
            }
        }
    }

    @Test
    void workSpotReachesEverySpotOverTime() {
        Set<Integer> seen = new HashSet<>();
        for (int roll = 0; roll < 4; roll++) {
            seen.add(DalekGarrisonLogic.nextWorkSpot(0, 5, roll));
        }
        assertEquals(Set.of(1, 2, 3, 4), seen);
        assertEquals(0, DalekGarrisonLogic.nextWorkSpot(0, 1, 7));
        assertEquals(0, DalekGarrisonLogic.nextWorkSpot(0, 0, 7));
    }

    @Test
    void leashDropsTheTargetOnlyBeyondTheLeash() {
        assertFalse(DalekGarrisonLogic.shouldDropTarget(39.9, DalekGarrisonLogic.LEASH_DISTANCE));
        assertFalse(DalekGarrisonLogic.shouldDropTarget(40.0, DalekGarrisonLogic.LEASH_DISTANCE));
        assertTrue(DalekGarrisonLogic.shouldDropTarget(40.1, DalekGarrisonLogic.LEASH_DISTANCE));
    }

    @Test
    void arrivalIsInclusiveOfTheArriveDistance() {
        double arrive = DalekGarrisonLogic.ARRIVE_DISTANCE;
        assertTrue(DalekGarrisonLogic.hasArrived(arrive * arrive, arrive));
        assertFalse(DalekGarrisonLogic.hasArrived(arrive * arrive + 0.01, arrive));
    }

    @Test
    void flightIsUsedForHighPostsAndUnreachablePosts() {
        assertTrue(DalekGarrisonLogic.shouldFlyToPost(3.0, true));
        assertTrue(DalekGarrisonLogic.shouldFlyToPost(0.0, false));
        assertFalse(DalekGarrisonLogic.shouldFlyToPost(2.5, true));
        assertFalse(DalekGarrisonLogic.shouldFlyToPost(-4.0, true));
    }

    @Test
    void onlyLookoutsUseTheWideAlarmRadius() {
        for (DalekJob job : DalekJob.values()) {
            double expected = job == DalekJob.LOOKOUT
                    ? DalekGarrisonLogic.LOOKOUT_ALARM_RADIUS
                    : DalekGarrisonLogic.ALARM_RADIUS;
            assertEquals(expected, DalekGarrisonLogic.alarmRadius(
                    job, DalekGarrisonLogic.ALARM_RADIUS, DalekGarrisonLogic.LOOKOUT_ALARM_RADIUS), job.name());
        }
    }

    @Test
    void pauseTicksStayInsideTheRange() {
        assertEquals(40, DalekGarrisonLogic.pauseTicks(40, 80, 0.0F));
        assertEquals(80, DalekGarrisonLogic.pauseTicks(40, 80, 0.9999F));
        int mid = DalekGarrisonLogic.pauseTicks(40, 80, 0.5F);
        assertTrue(mid >= 40 && mid <= 80);
    }

    @Test
    void scanSweepOscillatesWithinTheSweepAngle() {
        assertEquals(0.0F, DalekGarrisonLogic.scanYawOffset(0), 0.001F);
        boolean sawPositive = false;
        boolean sawNegative = false;
        for (int tick = 0; tick < 400; tick++) {
            float offset = DalekGarrisonLogic.scanYawOffset(tick);
            assertTrue(Math.abs(offset) <= DalekGarrisonLogic.SCAN_SWEEP_DEGREES + 0.001F);
            sawPositive |= offset > 1.0F;
            sawNegative |= offset < -1.0F;
        }
        assertTrue(sawPositive && sawNegative);
    }

    @Test
    void onlySentriesAndLookoutsHoldAPost() {
        assertTrue(DalekJob.SENTRY.holdsPost());
        assertTrue(DalekJob.LOOKOUT.holdsPost());
        assertFalse(DalekJob.PATROL.holdsPost());
        assertFalse(DalekJob.TECHNICIAN.holdsPost());
        assertFalse(DalekJob.NONE.holdsPost());
    }

    @Test
    void jobsRoundTripThroughIdAndOrdinal() {
        for (DalekJob job : DalekJob.values()) {
            assertEquals(job, DalekJob.byId(job.getSerializedName()));
            assertEquals(job, DalekJob.byOrdinal(job.ordinal()));
        }
        assertEquals(DalekJob.NONE, DalekJob.byId("garbage"));
        assertEquals(DalekJob.NONE, DalekJob.byOrdinal(-1));
        assertEquals(DalekJob.NONE, DalekJob.byOrdinal(99));
    }
}
