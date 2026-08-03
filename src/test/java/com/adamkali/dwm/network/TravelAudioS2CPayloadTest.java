package com.adamkali.dwm.network;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TravelAudioS2CPayloadTest {
    @Test
    void actionConstants_areDistinct() {
        assertEquals(0, TravelAudioS2CPayload.START_DEMAT);
        assertEquals(1, TravelAudioS2CPayload.START_MAT);
        assertEquals(2, TravelAudioS2CPayload.STOP);
        assertEquals(3, TravelAudioS2CPayload.START_FLIGHT);
        assertNotEquals(TravelAudioS2CPayload.START_DEMAT, TravelAudioS2CPayload.START_FLIGHT);
        assertNotEquals(TravelAudioS2CPayload.START_MAT, TravelAudioS2CPayload.START_FLIGHT);
        assertNotEquals(TravelAudioS2CPayload.STOP, TravelAudioS2CPayload.START_FLIGHT);
    }

    @Test
    void record_preservesFlightAction() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000042");
        TravelAudioS2CPayload payload = new TravelAudioS2CPayload(
                id,
                TravelAudioS2CPayload.START_FLIGHT,
                Identifier.of("dwm", "tardis"),
                new BlockPos(5, 1, 5),
                true
        );
        assertEquals(TravelAudioS2CPayload.START_FLIGHT, payload.action());
        assertEquals(true, payload.relative());
        assertEquals(TravelAudioS2CPayload.ID, payload.getId());
    }
}
