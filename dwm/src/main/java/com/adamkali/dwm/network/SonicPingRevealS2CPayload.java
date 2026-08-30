package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Owner-only S2C cue to silhouette a cloaked exterior for {@link com.adamkali.dwm.item.SonicPingLogic#REVEAL_TICKS}.
 */
public record SonicPingRevealS2CPayload(UUID tardisId, BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SonicPingRevealS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SONIC_PING_REVEAL_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SonicPingRevealS2CPayload> CODEC =
            StreamCodec.composite(
                    DWMPacketCodecs.UUID_PACKET_CODEC, SonicPingRevealS2CPayload::tardisId,
                    BlockPos.STREAM_CODEC, SonicPingRevealS2CPayload::pos,
                    SonicPingRevealS2CPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
