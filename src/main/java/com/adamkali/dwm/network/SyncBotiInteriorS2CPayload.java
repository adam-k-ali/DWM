package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiInteriorSnapshot;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * S2C full BOTI footprint snapshot. formatVersion 1 = blocks only (BE list reserved for later).
 */
public record SyncBotiInteriorS2CPayload(
        byte formatVersion,
        UUID tardisId,
        int revision,
        List<Entry> blocks
) implements CustomPayload {
    public static final CustomPayload.Id<SyncBotiInteriorS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_BOTI_INTERIOR_PACKET_ID);

    public record Entry(short packedPos, int stateId) {
    }

    public static final PacketCodec<RegistryByteBuf, Entry> ENTRY_CODEC = PacketCodec.tuple(
            PacketCodecs.SHORT, Entry::packedPos,
            PacketCodecs.VAR_INT, Entry::stateId,
            Entry::new
    );

    public static final PacketCodec<RegistryByteBuf, SyncBotiInteriorS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE, SyncBotiInteriorS2CPayload::formatVersion,
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncBotiInteriorS2CPayload::tardisId,
            PacketCodecs.VAR_INT, SyncBotiInteriorS2CPayload::revision,
            ENTRY_CODEC.collect(PacketCodecs.toList()), SyncBotiInteriorS2CPayload::blocks,
            SyncBotiInteriorS2CPayload::new
    );

    public static SyncBotiInteriorS2CPayload fromSnapshot(BotiInteriorSnapshot snapshot) {
        List<Entry> entries = new ArrayList<>(snapshot.blocks().size());
        for (Map.Entry<BlockPos, BlockState> e : snapshot.blocks().entrySet()) {
            entries.add(new Entry(BotiRelativePosCodec.pack(e.getKey()), BotiRelativePosCodec.stateId(e.getValue())));
        }
        return new SyncBotiInteriorS2CPayload(
                (byte) snapshot.formatVersion(),
                snapshot.tardisId(),
                snapshot.revision(),
                entries
        );
    }

    public Map<BlockPos, BlockState> toBlockMap() {
        Map<BlockPos, BlockState> map = new HashMap<>(blocks.size());
        for (Entry entry : blocks) {
            BlockState state = BotiRelativePosCodec.stateFromId(entry.stateId());
            if (state != null && !state.isAir()) {
                map.put(BotiRelativePosCodec.unpack(entry.packedPos()), state);
            }
        }
        return map;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
