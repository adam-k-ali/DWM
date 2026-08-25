package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SaveWaypointC2SPayload(UUID tardisId, String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SaveWaypointC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SAVE_WAYPOINT_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveWaypointC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, SaveWaypointC2SPayload::tardisId,
            ByteBufCodecs.STRING_UTF8, SaveWaypointC2SPayload::name,
            SaveWaypointC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
