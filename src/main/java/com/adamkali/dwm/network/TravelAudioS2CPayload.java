package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * S2C cue to start/stop looping dematerialise, materialise, or in-flight travel audio at a position.
 */
public record TravelAudioS2CPayload(
        UUID tardisId,
        byte action,
        Identifier dimensionId,
        BlockPos pos,
        boolean relative
) implements CustomPayload {
    public static final byte START_DEMAT = 0;
    public static final byte START_MAT = 1;
    public static final byte STOP = 2;
    public static final byte START_FLIGHT = 3;

    public static final CustomPayload.Id<TravelAudioS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.TRAVEL_AUDIO_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, TravelAudioS2CPayload> CODEC =
            PacketCodec.tuple(
                    DWMPacketCodecs.UUID_PACKET_CODEC, TravelAudioS2CPayload::tardisId,
                    PacketCodecs.BYTE, TravelAudioS2CPayload::action,
                    Identifier.PACKET_CODEC, TravelAudioS2CPayload::dimensionId,
                    BlockPos.PACKET_CODEC, TravelAudioS2CPayload::pos,
                    PacketCodecs.BOOLEAN, TravelAudioS2CPayload::relative,
                    TravelAudioS2CPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
