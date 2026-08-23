package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RenameWaypointC2SPayload(UUID tardisId, UUID waypointId, String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenameWaypointC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.RENAME_WAYPOINT_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RenameWaypointC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, RenameWaypointC2SPayload::tardisId,
            DWMPacketCodecs.UUID_PACKET_CODEC, RenameWaypointC2SPayload::waypointId,
            ByteBufCodecs.STRING_UTF8, RenameWaypointC2SPayload::name,
            RenameWaypointC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
