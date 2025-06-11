package com.adamkali.dwm.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.UUID;

public class DWMPacketCodecs {
    public static final PacketCodec<ByteBuf, UUID> UUID_PACKET_CODEC = PacketCodec.of((uuid, buf) -> {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }, buf -> new UUID(buf.readLong(), buf.readLong()));
}
