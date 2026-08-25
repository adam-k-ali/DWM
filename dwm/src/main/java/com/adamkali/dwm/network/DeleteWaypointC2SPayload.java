package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DeleteWaypointC2SPayload(UUID tardisId, UUID waypointId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeleteWaypointC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.DELETE_WAYPOINT_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteWaypointC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, DeleteWaypointC2SPayload::tardisId,
            DWMPacketCodecs.UUID_PACKET_CODEC, DeleteWaypointC2SPayload::waypointId,
            DeleteWaypointC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
