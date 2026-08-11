package com.adamkali.dwm.tardis.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalStreamKindTest {

    @Test
    void wireRoundTrip() {
        for (PortalStreamKind kind : PortalStreamKind.values()) {
            assertEquals(kind, PortalStreamKind.fromWire(kind.toWire()));
        }
    }

    @Test
    void fromWire_rejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> PortalStreamKind.fromWire((byte) 99));
    }
}
