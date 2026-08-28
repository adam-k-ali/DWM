package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.Reading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicScanLogicTest {
    @Test
    void overlay_startsWithScanPrefixAndIncludesShipStatus() {
        TardisDataModel model = new TardisDataModel();
        model.doorsLocked = true;
        model.cloaked = false;
        model.setTravelPhase(TardisTravelPhase.IDLE);
        model.artron = 250;
        Reading reading = new Reading(false, 1.0F, 0.5F, 0.4F, 0.12F);
        String overlay = SonicScanLogic.PREFIX + SonicScanLogic.body(model, reading, false);
        assertTrue(overlay.startsWith("Scan:"));
        assertTrue(overlay.contains("Locked: yes"));
        assertTrue(overlay.contains("Cloaked: no"));
        assertTrue(overlay.contains("Artron reserves"));
        assertTrue(overlay.contains("Oxygen:"));
    }

    @Test
    void overlay_keepsPrefixWhenEnvironmentHasNoSignal() {
        TardisDataModel model = new TardisDataModel();
        String body = SonicScanLogic.body(model, Reading.none(), false);
        assertTrue(body.contains("No exterior signal"));
        assertTrue((SonicScanLogic.PREFIX + body).startsWith("Scan:"));
    }
}
