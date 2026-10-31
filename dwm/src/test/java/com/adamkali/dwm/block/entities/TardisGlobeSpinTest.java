package com.adamkali.dwm.block.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisGlobeSpinTest {
    private static final float EPSILON = 1.0e-3F;

    @Test
    void idle_isRestPose() {
        TardisGlobeSpin spin = new TardisGlobeSpin();
        assertEquals(0.0F, spin.degrees(1234L, 0.5F), EPSILON);
        assertEquals(0.0F, spin.energy(1234L, 0.5F), EPSILON);
        assertFalse(spin.isSpinning(1234L, 0.5F));
    }

    @Test
    void spin_turnsThreeTimesAndSettles() {
        TardisGlobeSpin spin = new TardisGlobeSpin();
        spin.start(100L);
        assertEquals(0.0F, spin.degrees(100L, 0.0F), EPSILON);
        assertTrue(spin.isSpinning(100L, 0.5F));

        long end = 100L + TardisGlobeSpin.DURATION_TICKS;
        assertEquals(TardisGlobeSpin.TURNS_DEGREES, spin.degrees(end, 0.0F), EPSILON);
        assertEquals(TardisGlobeSpin.TURNS_DEGREES, spin.degrees(end + 500L, 0.0F), EPSILON);
        assertEquals(0.0F, spin.energy(end, 0.0F), EPSILON);
        assertFalse(spin.isSpinning(end, 0.0F));
    }

    @Test
    void spin_degreesNeverRunBackwardsAndDecelerate() {
        TardisGlobeSpin spin = new TardisGlobeSpin();
        spin.start(0L);
        float previous = 0.0F;
        float previousStep = Float.MAX_VALUE;
        for (int tick = 1; tick <= TardisGlobeSpin.DURATION_TICKS; tick++) {
            float degrees = spin.degrees(tick, 0.0F);
            float step = degrees - previous;
            assertTrue(step >= 0.0F, "tick " + tick);
            assertTrue(step <= previousStep + EPSILON, "ease-out at tick " + tick);
            previous = degrees;
            previousStep = step;
        }
    }

    @Test
    void energy_peaksEarlyAndDecays() {
        TardisGlobeSpin spin = new TardisGlobeSpin();
        spin.start(0L);
        int peakTick = 0;
        float peak = 0.0F;
        for (int tick = 0; tick <= TardisGlobeSpin.DURATION_TICKS; tick++) {
            float energy = spin.energy(tick, 0.0F);
            assertTrue(energy >= 0.0F && energy <= 1.0F, "tick " + tick);
            if (energy > peak) {
                peak = energy;
                peakTick = tick;
            }
        }
        assertTrue(peak > 0.7F, "peak " + peak);
        assertTrue(peakTick < TardisGlobeSpin.DURATION_TICKS / 4, "peak tick " + peakTick);
    }

    @Test
    void restart_midSpin_keepsAngleContinuous() {
        TardisGlobeSpin spin = new TardisGlobeSpin();
        spin.start(0L);
        float before = spin.degrees(20L, 0.0F);
        spin.start(20L);
        assertEquals(before % 360.0F, spin.degrees(20L, 0.0F), EPSILON);
        assertTrue(spin.isSpinning(20L, 0.5F));
        assertEquals(before % 360.0F + TardisGlobeSpin.TURNS_DEGREES,
                spin.degrees(20L + TardisGlobeSpin.DURATION_TICKS, 0.0F), EPSILON);
    }

    @Test
    void restart_afterSpin_startsFromRestingAngle() {
        TardisGlobeSpin spin = new TardisGlobeSpin();
        spin.start(0L);
        spin.start(1000L);
        // 1080 degrees is a whole number of turns, so the globe rests where it started.
        assertEquals(0.0F, spin.degrees(1000L, 0.0F), EPSILON);
    }
}
