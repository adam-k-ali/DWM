package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** S2C unload one ghost exterior chunk column. */
public record UnloadSotoExteriorChunkS2CPayload(UUID tardisId, int chunkX, int chunkZ) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UnloadSotoExteriorChunkS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.UNLOAD_SOTO_EXTERIOR_CHUNK_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UnloadSotoExteriorChunkS2CPayload> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, UnloadSotoExteriorChunkS2CPayload::tardisId,
            ByteBufCodecs.VAR_INT, UnloadSotoExteriorChunkS2CPayload::chunkX,
            ByteBufCodecs.VAR_INT, UnloadSotoExteriorChunkS2CPayload::chunkZ,
            UnloadSotoExteriorChunkS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
