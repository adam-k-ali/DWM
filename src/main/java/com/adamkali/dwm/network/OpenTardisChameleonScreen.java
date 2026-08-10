package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenTardisChameleonScreen(UUID tardisId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenTardisChameleonScreen> ID = new CustomPacketPayload.Type<>(DWMPacketIds.OPEN_TARDIS_CHAMELEON_SCREEN_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTardisChameleonScreen> CODEC = StreamCodec.composite(DWMPacketCodecs.UUID_PACKET_CODEC, OpenTardisChameleonScreen::tardisId, OpenTardisChameleonScreen::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
