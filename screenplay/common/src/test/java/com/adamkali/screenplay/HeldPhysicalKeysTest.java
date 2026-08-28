package com.adamkali.screenplay;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeldPhysicalKeysTest {
    @AfterEach
    void tearDown() {
        HeldPhysicalKeys.clear();
    }

    @Test
    void hold_isVisibleUntilReleased() {
        assertFalse(HeldPhysicalKeys.isHeld(263));
        HeldPhysicalKeys.hold(263);
        assertTrue(HeldPhysicalKeys.isHeld(263));
        HeldPhysicalKeys.release(263);
        assertFalse(HeldPhysicalKeys.isHeld(263));
    }
}
