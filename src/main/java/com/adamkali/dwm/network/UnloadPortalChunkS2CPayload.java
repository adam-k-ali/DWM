package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** S2C unload a portal stream chunk column. */
public record UnloadPortalChunkS2CPayload(
        PortalStreamKind kind,
        UUID tardisId,
        int chunkX,
        int chunkZ
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UnloadPortalChunkS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.UNLOAD_PORTAL_CHUNK_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UnloadPortalChunkS2CPayload> CODEC =
            StreamCodec.ofMember(UnloadPortalChunkS2CPayload::encode, UnloadPortalChunkS2CPayload::decode);

    private static void encode(UnloadPortalChunkS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.kind.toWire());
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        ByteBufCodecs.VAR_INT.encode(buf, payload.chunkX);
        ByteBufCodecs.VAR_INT.encode(buf, payload.chunkZ);
    }

    private static UnloadPortalChunkS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new UnloadPortalChunkS2CPayload(
                PortalStreamKind.fromWire(buf.readByte()),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
