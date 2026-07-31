package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

/**
 * C2S request for a BOTI interior snapshot when the client has no (or stale) cache entry.
 */
public record RequestBotiInteriorC2SPayload(UUID tardisId) implements CustomPayload {
    public static final CustomPayload.Id<RequestBotiInteriorC2SPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.REQUEST_BOTI_INTERIOR_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, RequestBotiInteriorC2SPayload> CODEC = PacketCodec.tuple(
            DWMPacketCodecs.UUID_PACKET_CODEC, RequestBotiInteriorC2SPayload::tardisId,
            RequestBotiInteriorC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
