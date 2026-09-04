package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekFlightFxTest {
    private static final double EPSILON = 0.001;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void exhaustPosStaysUnderTheSkirt() {
        RandomSource random = RandomSource.create(42L);
        Vec3 origin = new Vec3(10.0, 64.0, -3.0);
        for (int i = 0; i < 80; i++) {
            Vec3 pos = DalekFlightFx.exhaustPos(origin, random);
            double dx = pos.x - origin.x;
            double dz = pos.z - origin.z;
            assertEquals(origin.y + DalekFlightFx.EXHAUST_Y, pos.y, EPSILON);
            assertTrue(Math.hypot(dx, dz) <= DalekFlightFx.EXHAUST_RADIUS + EPSILON);
        }
    }

    @Test
    void exhaustVelocityPointsDownwardAndLagsBehindMotion() {
        RandomSource random = RandomSource.create(7L);
        Vec3 movement = new Vec3(0.4, 0.1, 0.0);
        for (int i = 0; i < 40; i++) {
            Vec3 velocity = DalekFlightFx.exhaustVelocity(movement, random);
            assertTrue(velocity.y < 0.0, "exhaust must push down");
            assertTrue(velocity.x < 0.0, "wake should lag opposite +X travel");
        }
    }

    @Test
    void cruiseCountIncreasesWithSpeedAndClimbing() {
        int hover = DalekFlightFx.cruiseParticleCount(0.0, false);
        int cruise = DalekFlightFx.cruiseParticleCount(0.2, false);
        int climb = DalekFlightFx.cruiseParticleCount(0.0, true);
        int fast = DalekFlightFx.cruiseParticleCount(0.4, false);
        assertEquals(1, hover);
        assertTrue(cruise > hover);
        assertTrue(climb > hover);
        assertEquals(3, fast);
        assertTrue(DalekFlightFx.takeoffBurstCount() > fast);
    }

    @Test
    void leanIsZeroWhenNotFlying() {
        Vec3 fastForward = new Vec3(0.0, 0.0, 0.4);
        assertEquals(0.0F, DalekFlightFx.leanPitchDegrees(fastForward, false), EPSILON);
        assertEquals(0.0F, DalekFlightFx.leanRollDegrees(fastForward, false), EPSILON);
    }

    @Test
    void leanIsNearZeroAtHoverSpeed() {
        Vec3 hover = new Vec3(0.01, 0.0, 0.01);
        assertTrue(Math.abs(DalekFlightFx.leanPitchDegrees(hover, true)) < 1.0F);
        assertTrue(Math.abs(DalekFlightFx.leanRollDegrees(hover, true)) < 1.0F);
    }

    @Test
    void leanIncreasesTowardClampWhenFlyingFast() {
        Vec3 forward = new Vec3(0.0, 0.0, 0.4);
        Vec3 strafe = new Vec3(0.4, 0.0, 0.0);
        float pitch = DalekFlightFx.leanPitchDegrees(forward, true);
        float roll = DalekFlightFx.leanRollDegrees(strafe, true);
        assertEquals(DalekFlightFx.MAX_PITCH_DEGREES, pitch, EPSILON);
        assertEquals(DalekFlightFx.MAX_ROLL_DEGREES, roll, EPSILON);

        Vec3 overspeed = new Vec3(0.0, 0.0, 1.0);
        assertEquals(
                DalekFlightFx.MAX_PITCH_DEGREES,
                DalekFlightFx.leanPitchDegrees(overspeed, true),
                EPSILON
        );
    }

    @Test
    void toLocalVelocityMapsSouthFacingForwardAsPositiveZ() {
        Vec3 south = new Vec3(0.0, 0.0, 0.4);
        Vec3 local = DalekFlightFx.toLocalVelocity(south, 0.0F);
        assertEquals(0.0, local.x, EPSILON);
        assertEquals(0.4, local.z, EPSILON);
    }

    @Test
    void bobOffsetIsZeroOnGroundAndOscillatesInFlight() {
        assertEquals(0.0F, DalekFlightFx.bobOffset(12.5F, false), EPSILON);
        float trough = DalekFlightFx.bobOffset((float) ((3.0 * Math.PI / 2.0) / DalekFlightFx.BOB_SPEED), true);
        assertEquals(-DalekFlightFx.BOB_AMPLITUDE, trough, 0.01F);
        for (int i = 0; i <= 40; i++) {
            float offset = DalekFlightFx.bobOffset(i, true);
            assertTrue(offset <= DalekFlightFx.BOB_AMPLITUDE + 0.01F);
            assertTrue(offset >= -DalekFlightFx.BOB_AMPLITUDE - 0.01F);
        }
    }
}
