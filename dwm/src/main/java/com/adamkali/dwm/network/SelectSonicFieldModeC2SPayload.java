package com.adamkali.dwm.network;

import com.adamkali.dwm.item.SonicFieldMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectSonicFieldModeC2SPayload(SonicFieldMode mode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectSonicFieldModeC2SPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SELECT_SONIC_FIELD_MODE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectSonicFieldModeC2SPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.fromCodec(SonicFieldMode.CODEC).cast(),
                    SelectSonicFieldModeC2SPayload::mode,
                    SelectSonicFieldModeC2SPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
