package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C cue to start/stop looping dematerialise, materialise, or in-flight travel audio at a position.
 */
public record TravelAudioS2CPayload(
        UUID tardisId,
        byte action,
        Identifier dimensionId,
        BlockPos pos,
        boolean relative
) implements CustomPacketPayload {
    public static final byte START_DEMAT = 0;
    public static final byte START_MAT = 1;
    public static final byte STOP = 2;
    public static final byte START_FLIGHT = 3;

    public static final CustomPacketPayload.Type<TravelAudioS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.TRAVEL_AUDIO_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TravelAudioS2CPayload> CODEC =
            StreamCodec.composite(
                    DWMPacketCodecs.UUID_PACKET_CODEC, TravelAudioS2CPayload::tardisId,
                    ByteBufCodecs.BYTE, TravelAudioS2CPayload::action,
                    Identifier.STREAM_CODEC, TravelAudioS2CPayload::dimensionId,
                    BlockPos.STREAM_CODEC, TravelAudioS2CPayload::pos,
                    ByteBufCodecs.BOOL, TravelAudioS2CPayload::relative,
                    TravelAudioS2CPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
