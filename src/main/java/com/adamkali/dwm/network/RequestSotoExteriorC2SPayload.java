package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record RequestSotoExteriorC2SPayload(UUID tardisId) implements CustomPayload {
    public static final CustomPayload.Id<RequestSotoExteriorC2SPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.REQUEST_SOTO_EXTERIOR_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, RequestSotoExteriorC2SPayload> CODEC = PacketCodec.tuple(
            DWMPacketCodecs.UUID_PACKET_CODEC, RequestSotoExteriorC2SPayload::tardisId,
            RequestSotoExteriorC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
