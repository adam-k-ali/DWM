package com.adamkali.dwm.tardis.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonitorPageTest {
    @Test
    void next_cyclesInDeclarationOrderAndWraps() {
        assertEquals(MonitorPage.DESTINATION, MonitorPage.LOCATION.next());
        assertEquals(MonitorPage.SYSTEMS, MonitorPage.DESTINATION.next());
        assertEquals(MonitorPage.ATMOSPHERE, MonitorPage.SYSTEMS.next());
        assertEquals(MonitorPage.LOCATION, MonitorPage.ATMOSPHERE.next());
    }

    @Test
    void previous_undoesNext() {
        for (MonitorPage page : MonitorPage.values()) {
            assertEquals(page, page.next().previous());
        }
        assertEquals(MonitorPage.ATMOSPHERE, MonitorPage.LOCATION.previous());
    }

    @Test
    void serializedName_roundTripsAndFallsBackToLocation() {
        for (MonitorPage page : MonitorPage.values()) {
            assertEquals(page, MonitorPage.fromSerializedName(page.serializedName()));
        }
        assertEquals(MonitorPage.LOCATION, MonitorPage.fromSerializedName(null));
        assertEquals(MonitorPage.LOCATION, MonitorPage.fromSerializedName("bogus"));
        assertEquals("dwm.monitor.page.systems", MonitorPage.SYSTEMS.titleKey());
    }
}
