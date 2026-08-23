package com.adamkali.dwm.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class DWMPacketCodecs {
    public static final StreamCodec<ByteBuf, UUID> UUID_PACKET_CODEC = StreamCodec.ofMember((uuid, buf) -> {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }, buf -> new UUID(buf.readLong(), buf.readLong()));

    /** Nullable UUID encoded as a present flag followed by the UUID when present. */
    public static final StreamCodec<ByteBuf, @Nullable UUID> NULLABLE_UUID_PACKET_CODEC = StreamCodec.of(
            (buf, uuid) -> {
                buf.writeBoolean(uuid != null);
                if (uuid != null) {
                    UUID_PACKET_CODEC.encode(buf, uuid);
                }
            },
            buf -> buf.readBoolean() ? UUID_PACKET_CODEC.decode(buf) : null
    );
}
