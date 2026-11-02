package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CyberSpeedBurstTest {
    @Test
    void burstsAtADistantTargetWhenReady() {
        assertTrue(CyberSpeedBurst.shouldBurst(true, 10.0, 0));
        assertTrue(CyberSpeedBurst.shouldBurst(true, CyberSpeedBurst.MIN_DISTANCE, 0));
        assertTrue(CyberSpeedBurst.shouldBurst(true, CyberSpeedBurst.MAX_DISTANCE, 0));
    }

    @Test
    void doesNotBurstWithoutATarget() {
        assertFalse(CyberSpeedBurst.shouldBurst(false, 10.0, 0));
    }

    @Test
    void doesNotBurstWhenCloseOrOutOfRange() {
        assertFalse(CyberSpeedBurst.shouldBurst(true, CyberSpeedBurst.MIN_DISTANCE - 0.1, 0));
        assertFalse(CyberSpeedBurst.shouldBurst(true, CyberSpeedBurst.MAX_DISTANCE + 0.1, 0));
    }

    @Test
    void doesNotBurstDuringCooldown() {
        assertFalse(CyberSpeedBurst.shouldBurst(true, 10.0, 1));
        assertFalse(CyberSpeedBurst.shouldBurst(true, 10.0, CyberSpeedBurst.COOLDOWN_TICKS));
    }
}
