package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CybermasterRegenerationTest {
    @Test
    void regeneratesOnTheFirstKillingBlow() {
        assertTrue(CybermasterRegeneration.shouldRegenerate(true, false, false));
    }

    @Test
    void doesNotRegenerateWhileAlive() {
        assertFalse(CybermasterRegeneration.shouldRegenerate(false, false, false));
    }

    @Test
    void regeneratesOnlyOnce() {
        assertFalse(CybermasterRegeneration.shouldRegenerate(true, true, false));
    }

    @Test
    void killAndVoidDamageAreNeverSurvived() {
        assertFalse(CybermasterRegeneration.shouldRegenerate(true, false, true));
    }
}
