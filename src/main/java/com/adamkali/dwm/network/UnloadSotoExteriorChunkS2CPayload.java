package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

/** S2C unload one ghost exterior chunk column. */
public record UnloadSotoExteriorChunkS2CPayload(UUID tardisId, int chunkX, int chunkZ) implements CustomPayload {
    public static final CustomPayload.Id<UnloadSotoExteriorChunkS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.UNLOAD_SOTO_EXTERIOR_CHUNK_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, UnloadSotoExteriorChunkS2CPayload> CODEC = PacketCodec.tuple(
            DWMPacketCodecs.UUID_PACKET_CODEC, UnloadSotoExteriorChunkS2CPayload::tardisId,
            PacketCodecs.VAR_INT, UnloadSotoExteriorChunkS2CPayload::chunkX,
            PacketCodecs.VAR_INT, UnloadSotoExteriorChunkS2CPayload::chunkZ,
            UnloadSotoExteriorChunkS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
