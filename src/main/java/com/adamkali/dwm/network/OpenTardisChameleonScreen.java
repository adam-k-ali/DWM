package com.adamkali.dwm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record OpenTardisChameleonScreen(UUID tardisId) implements CustomPayload {
    public static final CustomPayload.Id<OpenTardisChameleonScreen> ID = new CustomPayload.Id<>(DWMPacketIds.OPEN_TARDIS_CHAMELEON_SCREEN_ID);
    public static final PacketCodec<RegistryByteBuf, OpenTardisChameleonScreen> CODEC = PacketCodec.tuple(DWMPacketCodecs.UUID_PACKET_CODEC, OpenTardisChameleonScreen::tardisId, OpenTardisChameleonScreen::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
