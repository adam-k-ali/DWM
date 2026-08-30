package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.Reading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicScanLogicTest {
    @Test
    void overlay_startsWithScanPrefixAndIncludesLockStatus() {
        TardisDataModel model = new TardisDataModel();
        model.doorsLocked = true;
        model.cloaked = false;
        model.setTravelPhase(TardisTravelPhase.IDLE);
        model.artron = 250;
        Reading reading = new Reading(false, 1.0F, 0.5F, 0.4F, 0.12F);
        SonicScanLogic.Snapshot snapshot = SonicScanLogic.snapshot(model, reading, false);
        String overlay = SonicScanLogic.overlay(snapshot).getString();
        assertTrue(overlay.startsWith("Scan:"));
        assertTrue(overlay.contains("Locked: yes"));
        assertFalse(overlay.contains("Oxygen:"));
        assertEquals(100, snapshot.oxygen());
        assertEquals(40, snapshot.temperature());
        assertEquals(12, snapshot.radiation());
        assertFalse(snapshot.cloaked());
        assertEquals(TardisTravelPhase.IDLE, snapshot.phase());
        assertFalse(snapshot.artronEmpty());
    }

    @Test
    void overlay_keepsPrefixWhenEnvironmentHasNoSignal() {
        TardisDataModel model = new TardisDataModel();
        SonicScanLogic.Snapshot snapshot = SonicScanLogic.snapshot(model, Reading.none(), false);
        assertTrue(snapshot.noSignal());
        String overlay = SonicScanLogic.overlay(snapshot).getString();
        assertTrue(overlay.startsWith("Scan:"));
        assertTrue(overlay.contains("Locked:"));
    }
}
