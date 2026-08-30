package com.adamkali.dwm.network;

import com.adamkali.dwm.item.SonicScanLogic;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Owner-only S2C snapshot for the sonic Scan HUD panel.
 */
public record SonicScanS2CPayload(SonicScanLogic.Snapshot snapshot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SonicScanS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SONIC_SCAN_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SonicScanS2CPayload> CODEC =
            StreamCodec.ofMember(SonicScanS2CPayload::encode, SonicScanS2CPayload::decode);

    private static void encode(SonicScanS2CPayload payload, RegistryFriendlyByteBuf buf) {
        SonicScanLogic.Snapshot snapshot = payload.snapshot;
        buf.writeBoolean(snapshot.noSignal());
        ByteBufCodecs.VAR_INT.encode(buf, snapshot.oxygen());
        ByteBufCodecs.VAR_INT.encode(buf, snapshot.temperature());
        ByteBufCodecs.VAR_INT.encode(buf, snapshot.radiation());
        buf.writeBoolean(snapshot.waterlogged());
        buf.writeBoolean(snapshot.locked());
        buf.writeBoolean(snapshot.cloaked());
        ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.phase().name());
        ByteBufCodecs.VAR_INT.encode(buf, snapshot.artronPercent());
        buf.writeBoolean(snapshot.artronEmpty());
    }

    private static SonicScanS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new SonicScanS2CPayload(new SonicScanLogic.Snapshot(
                buf.readBoolean(),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                TardisTravelPhase.fromString(ByteBufCodecs.STRING_UTF8.decode(buf)),
                ByteBufCodecs.VAR_INT.decode(buf),
                buf.readBoolean()
        ));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
