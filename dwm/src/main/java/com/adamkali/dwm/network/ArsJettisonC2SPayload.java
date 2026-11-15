package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Asks the server to jettison the ARS piece covering cell {@code (cellX, cellZ)}. Every field is untrusted;
 * the server re-checks all of them.
 */
public record ArsJettisonC2SPayload(UUID tardisId, int cellX, int cellZ) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ArsJettisonC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.ARS_JETTISON_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ArsJettisonC2SPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, ArsJettisonC2SPayload::tardisId,
            ByteBufCodecs.VAR_INT, ArsJettisonC2SPayload::cellX,
            ByteBufCodecs.VAR_INT, ArsJettisonC2SPayload::cellZ,
            ArsJettisonC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
