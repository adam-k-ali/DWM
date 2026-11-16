package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Asks the server to grow ARS piece {@code pieceId} anchored at cell {@code (cellX, cellZ)} with
 * {@code rotation} clockwise quarter turns. Every field is untrusted; the server re-checks all of them.
 */
public record ArsGrowC2SPayload(UUID tardisId, Identifier pieceId, int cellX, int cellZ, int rotation)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ArsGrowC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.ARS_GROW_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ArsGrowC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, ArsGrowC2SPayload::tardisId,
            Identifier.STREAM_CODEC, ArsGrowC2SPayload::pieceId,
            ByteBufCodecs.VAR_INT, ArsGrowC2SPayload::cellX,
            ByteBufCodecs.VAR_INT, ArsGrowC2SPayload::cellZ,
            ByteBufCodecs.VAR_INT, ArsGrowC2SPayload::rotation,
            ArsGrowC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
