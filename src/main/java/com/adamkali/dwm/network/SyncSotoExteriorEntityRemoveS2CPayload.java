package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** S2C remove a live ghost exterior entity. */
public record SyncSotoExteriorEntityRemoveS2CPayload(UUID tardisId, UUID entityUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSotoExteriorEntityRemoveS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_ENTITY_REMOVE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSotoExteriorEntityRemoveS2CPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncSotoExteriorEntityRemoveS2CPayload::tardisId,
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncSotoExteriorEntityRemoveS2CPayload::entityUuid,
            SyncSotoExteriorEntityRemoveS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
