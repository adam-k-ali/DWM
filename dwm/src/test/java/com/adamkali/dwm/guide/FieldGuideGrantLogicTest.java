package com.adamkali.dwm.guide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldGuideGrantLogicTest {
    @Test
    void shouldGrant_whenNotYetReceived() {
        assertTrue(FieldGuideGrantLogic.shouldGrant(false));
    }

    @Test
    void shouldGrant_falseWhenAlreadyReceived() {
        assertFalse(FieldGuideGrantLogic.shouldGrant(true));
    }
}
