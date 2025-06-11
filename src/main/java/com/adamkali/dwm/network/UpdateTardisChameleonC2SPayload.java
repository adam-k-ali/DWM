package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record UpdateTardisChameleonC2SPayload(Identifier variantId, UUID tardisId) implements CustomPayload {
    public static final CustomPayload.Id<UpdateTardisChameleonC2SPayload> ID = new CustomPayload.Id<>(DWMPacketIds.UPDATE_TARDIS_CHAMELEON_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, UpdateTardisChameleonC2SPayload> CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, UpdateTardisChameleonC2SPayload::variantId,
            DWMPacketCodecs.UUID_PACKET_CODEC, UpdateTardisChameleonC2SPayload::tardisId,
            UpdateTardisChameleonC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
