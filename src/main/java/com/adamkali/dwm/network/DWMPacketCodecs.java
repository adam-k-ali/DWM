package com.adamkali.dwm.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;

public class DWMPacketCodecs {
    public static final StreamCodec<ByteBuf, UUID> UUID_PACKET_CODEC = StreamCodec.ofMember((uuid, buf) -> {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }, buf -> new UUID(buf.readLong(), buf.readLong()));
}
