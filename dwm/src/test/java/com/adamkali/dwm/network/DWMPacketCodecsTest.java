package com.adamkali.dwm.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DWMPacketCodecsTest {

    @Test
    void testUUIDCodecRoundTrip() {
        UUID originalUUID = UUID.randomUUID();

        ByteBuf buffer = Unpooled.buffer();

        try {
            DWMPacketCodecs.UUID_PACKET_CODEC.encode(buffer, originalUUID);

            // Verify buffer size is 16 bytes (UUID is 16 bytes)
            assertEquals(16, buffer.readableBytes(), "Buffer should contain exactly 16 bytes");

            UUID decodedUUID = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buffer);

            assertEquals(originalUUID, decodedUUID, "Decoded UUID should match original UUID");
        } finally {
            buffer.release();
        }
    }
}
