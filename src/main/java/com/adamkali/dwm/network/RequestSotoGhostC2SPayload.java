package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

/** C2S subscribe / refresh for Phase 1 ghost exterior stream (fixed stream radius). */
public record RequestSotoGhostC2SPayload(UUID tardisId) implements CustomPayload {
    public static final CustomPayload.Id<RequestSotoGhostC2SPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.REQUEST_SOTO_GHOST_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, RequestSotoGhostC2SPayload> CODEC = PacketCodec.tuple(
            DWMPacketCodecs.UUID_PACKET_CODEC, RequestSotoGhostC2SPayload::tardisId,
            RequestSotoGhostC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
