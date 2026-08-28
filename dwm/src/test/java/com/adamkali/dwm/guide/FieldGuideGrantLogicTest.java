package com.adamkali.dwm.guide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void slotForGrant_prefersLastHotbarSlotWhenEmpty() {
        assertEquals(FieldGuideGrantLogic.PREFERRED_HOTBAR_SLOT, FieldGuideGrantLogic.slotForGrant(true));
    }

    @Test
    void slotForGrant_fallsBackWhenPreferredOccupied() {
        assertEquals(FieldGuideGrantLogic.FALLBACK_TO_ADD, FieldGuideGrantLogic.slotForGrant(false));
    }
}
