package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectPlayerC2SPayload(UUID tardisId, UUID playerUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectPlayerC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SELECT_PLAYER_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectPlayerC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, SelectPlayerC2SPayload::tardisId,
            DWMPacketCodecs.UUID_PACKET_CODEC, SelectPlayerC2SPayload::playerUuid,
            SelectPlayerC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
