package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateTardisChameleonC2SPayload(Identifier variantId, UUID tardisId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateTardisChameleonC2SPayload> ID = new CustomPacketPayload.Type<>(DWMPacketIds.UPDATE_TARDIS_CHAMELEON_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTardisChameleonC2SPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, UpdateTardisChameleonC2SPayload::variantId,
            DWMPacketCodecs.UUID_PACKET_CODEC, UpdateTardisChameleonC2SPayload::tardisId,
            UpdateTardisChameleonC2SPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
