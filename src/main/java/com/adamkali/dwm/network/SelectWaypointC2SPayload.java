package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectWaypointC2SPayload(UUID tardisId, UUID waypointId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectWaypointC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SELECT_WAYPOINT_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectWaypointC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, SelectWaypointC2SPayload::tardisId,
            DWMPacketCodecs.UUID_PACKET_CODEC, SelectWaypointC2SPayload::waypointId,
            SelectWaypointC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
