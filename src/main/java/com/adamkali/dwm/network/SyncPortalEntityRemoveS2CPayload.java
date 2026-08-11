package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** S2C remove a portal ghost entity. */
public record SyncPortalEntityRemoveS2CPayload(
        PortalStreamKind kind,
        UUID tardisId,
        UUID entityUuid
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPortalEntityRemoveS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_ENTITY_REMOVE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalEntityRemoveS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalEntityRemoveS2CPayload::encode, SyncPortalEntityRemoveS2CPayload::decode);

    private static void encode(SyncPortalEntityRemoveS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.kind.toWire());
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.entityUuid);
    }

    private static SyncPortalEntityRemoveS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new SyncPortalEntityRemoveS2CPayload(
                PortalStreamKind.fromWire(buf.readByte()),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
