package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S request for portal meta + chunk/entity stream for a given kind. */
public record RequestPortalStreamC2SPayload(
        PortalStreamKind kind,
        UUID tardisId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestPortalStreamC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.REQUEST_PORTAL_STREAM_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPortalStreamC2SPayload> CODEC =
            StreamCodec.ofMember(RequestPortalStreamC2SPayload::encode, RequestPortalStreamC2SPayload::decode);

    private static void encode(RequestPortalStreamC2SPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.kind.toWire());
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
    }

    private static RequestPortalStreamC2SPayload decode(RegistryFriendlyByteBuf buf) {
        return new RequestPortalStreamC2SPayload(
                PortalStreamKind.fromWire(buf.readByte()),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
