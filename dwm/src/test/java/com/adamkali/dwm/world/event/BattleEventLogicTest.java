package com.adamkali.dwm.world.event;

import org.junit.jupiter.api.Test;

import static com.adamkali.dwm.world.event.BattleEventLogic.*;
import static org.junit.jupiter.api.Assertions.*;

class BattleEventLogicTest {
    @Test
    void shouldTrigger_requiresEligibilityNoActiveBattleAndCooldown() {
        long now = 100_000L;
        assertTrue(shouldTrigger(true, false, now, now - COOLDOWN_TICKS, 0.0));
        assertFalse(shouldTrigger(false, false, now, now - COOLDOWN_TICKS, 0.0));
        assertFalse(shouldTrigger(true, true, now, now - COOLDOWN_TICKS, 0.0));
        assertFalse(shouldTrigger(true, false, now, now - COOLDOWN_TICKS + 1, 0.0));
    }

    @Test
    void shouldTrigger_respectsChance() {
        long now = 100_000L;
        assertTrue(shouldTrigger(true, false, now, 0, TRIGGER_CHANCE - 0.001));
        assertFalse(shouldTrigger(true, false, now, 0, TRIGGER_CHANCE));
    }

    @Test
    void squadSizes_stayInRange() {
        for (double roll = 0.0; roll < 1.0; roll += 0.01) {
            assertTrue(daleks(roll) >= MIN_DALEKS && daleks(roll) <= MAX_DALEKS);
            assertTrue(reinforcements(roll) >= MIN_REINFORCEMENTS && reinforcements(roll) <= MAX_REINFORCEMENTS);
        }
        assertEquals(MIN_DALEKS, daleks(0.0));
        assertEquals(MAX_DALEKS, daleks(0.999));
        assertEquals(MAX_DALEKS, daleks(1.0));
    }

    @Test
    void spawnOffset_landsOnTheRing() {
        for (double angle = 0.0; angle < 1.0; angle += 0.07) {
            for (double dist = 0.0; dist <= 1.0; dist += 0.25) {
                Offset offset = spawnOffset(angle, dist);
                double d = Math.hypot(offset.dx(), offset.dz());
                assertTrue(d >= MIN_SPAWN_DISTANCE - 1.0 && d <= MAX_SPAWN_DISTANCE + 1.0, "distance " + d);
            }
        }
    }

    @Test
    void evaluate_decidesByWipeoutThenTimeout() {
        assertEquals(Outcome.ONGOING, evaluate(2, 3, 0));
        assertEquals(Outcome.THALS_WON, evaluate(0, 3, 10));
        assertEquals(Outcome.DALEKS_WON, evaluate(2, 0, 10));
        assertEquals(Outcome.THALS_WON, evaluate(0, 0, 10));
        assertEquals(Outcome.TIMED_OUT, evaluate(2, 3, TIMEOUT_TICKS));
        assertEquals(Outcome.ONGOING, evaluate(2, 3, TIMEOUT_TICKS - 1));
    }
}
