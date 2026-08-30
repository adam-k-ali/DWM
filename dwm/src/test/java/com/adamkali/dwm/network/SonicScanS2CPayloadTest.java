package com.adamkali.dwm.network;

import com.adamkali.dwm.item.SonicScanLogic;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicScanS2CPayloadTest {
    @Test
    void record_preservesSnapshot() {
        SonicScanLogic.Snapshot snapshot = new SonicScanLogic.Snapshot(
                false,
                80,
                40,
                12,
                true,
                true,
                false,
                TardisTravelPhase.IN_FLIGHT,
                50,
                false
        );
        SonicScanS2CPayload payload = new SonicScanS2CPayload(snapshot);
        assertEquals(SonicScanS2CPayload.ID, payload.type());
        assertEquals(80, payload.snapshot().oxygen());
        assertTrue(payload.snapshot().locked());
        assertFalse(payload.snapshot().cloaked());
        assertEquals(TardisTravelPhase.IN_FLIGHT, payload.snapshot().phase());
    }
}
