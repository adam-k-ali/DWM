package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * C2S request for a BOTI interior snapshot when the client has no (or stale) cache entry.
 */
public record RequestBotiInteriorC2SPayload(UUID tardisId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestBotiInteriorC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.REQUEST_BOTI_INTERIOR_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBotiInteriorC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, RequestBotiInteriorC2SPayload::tardisId,
            RequestBotiInteriorC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
