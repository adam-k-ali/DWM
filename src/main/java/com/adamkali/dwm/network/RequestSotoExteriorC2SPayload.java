package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestSotoExteriorC2SPayload(UUID tardisId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSotoExteriorC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.REQUEST_SOTO_EXTERIOR_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSotoExteriorC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, RequestSotoExteriorC2SPayload::tardisId,
            RequestSotoExteriorC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
