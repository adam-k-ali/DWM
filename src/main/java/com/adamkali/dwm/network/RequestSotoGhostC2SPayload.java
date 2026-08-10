package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S subscribe / refresh for Phase 1 ghost exterior stream (fixed stream radius). */
public record RequestSotoGhostC2SPayload(UUID tardisId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSotoGhostC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.REQUEST_SOTO_GHOST_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSotoGhostC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, RequestSotoGhostC2SPayload::tardisId,
            RequestSotoGhostC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
