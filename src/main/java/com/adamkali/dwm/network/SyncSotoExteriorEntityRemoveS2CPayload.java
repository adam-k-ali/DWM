package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

/** S2C remove a live ghost exterior entity. */
public record SyncSotoExteriorEntityRemoveS2CPayload(UUID tardisId, UUID entityUuid) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorEntityRemoveS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_ENTITY_REMOVE_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorEntityRemoveS2CPayload> CODEC = PacketCodec.tuple(
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncSotoExteriorEntityRemoveS2CPayload::tardisId,
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncSotoExteriorEntityRemoveS2CPayload::entityUuid,
            SyncSotoExteriorEntityRemoveS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
