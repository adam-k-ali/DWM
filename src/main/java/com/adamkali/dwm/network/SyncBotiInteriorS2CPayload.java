package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiInteriorSnapshot;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
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
 * S2C full BOTI footprint snapshot. formatVersion 3 = blocks + BE NBT + entities.
 */
public record SyncBotiInteriorS2CPayload(
        byte formatVersion,
        UUID tardisId,
        int revision,
        List<Entry> blocks,
        List<BlockEntityEntry> blockEntities,
        List<EntityEntry> entities
) implements CustomPayload {
    public static final CustomPayload.Id<SyncBotiInteriorS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_BOTI_INTERIOR_PACKET_ID);

    public record Entry(short packedPos, int stateId) {
    }

    public record BlockEntityEntry(short packedPos, NbtCompound nbt) {
    }

    public record EntityEntry(
            float relX,
            float relY,
            float relZ,
            float yaw,
            float pitch,
            NbtCompound nbt
    ) {
    }

    public static final PacketCodec<RegistryByteBuf, Entry> ENTRY_CODEC = PacketCodec.tuple(
            PacketCodecs.SHORT, Entry::packedPos,
            PacketCodecs.VAR_INT, Entry::stateId,
            Entry::new
    );

    public static final PacketCodec<RegistryByteBuf, BlockEntityEntry> BLOCK_ENTITY_ENTRY_CODEC = PacketCodec.tuple(
            PacketCodecs.SHORT, BlockEntityEntry::packedPos,
            PacketCodecs.NBT_COMPOUND, BlockEntityEntry::nbt,
            BlockEntityEntry::new
    );

    public static final PacketCodec<RegistryByteBuf, EntityEntry> ENTITY_ENTRY_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, EntityEntry::relX,
            PacketCodecs.FLOAT, EntityEntry::relY,
            PacketCodecs.FLOAT, EntityEntry::relZ,
            PacketCodecs.FLOAT, EntityEntry::yaw,
            PacketCodecs.FLOAT, EntityEntry::pitch,
            PacketCodecs.NBT_COMPOUND, EntityEntry::nbt,
            EntityEntry::new
    );

    public static final PacketCodec<RegistryByteBuf, SyncBotiInteriorS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE, SyncBotiInteriorS2CPayload::formatVersion,
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncBotiInteriorS2CPayload::tardisId,
            PacketCodecs.VAR_INT, SyncBotiInteriorS2CPayload::revision,
            ENTRY_CODEC.collect(PacketCodecs.toList()), SyncBotiInteriorS2CPayload::blocks,
            BLOCK_ENTITY_ENTRY_CODEC.collect(PacketCodecs.toList()), SyncBotiInteriorS2CPayload::blockEntities,
            ENTITY_ENTRY_CODEC.collect(PacketCodecs.toList()), SyncBotiInteriorS2CPayload::entities,
            SyncBotiInteriorS2CPayload::new
    );

    public static SyncBotiInteriorS2CPayload fromSnapshot(BotiInteriorSnapshot snapshot) {
        List<Entry> entries = new ArrayList<>(snapshot.blocks().size());
        for (Map.Entry<BlockPos, BlockState> e : snapshot.blocks().entrySet()) {
            entries.add(new Entry(BotiRelativePosCodec.pack(e.getKey()), BotiRelativePosCodec.stateId(e.getValue())));
        }
        List<BlockEntityEntry> beEntries = new ArrayList<>(snapshot.blockEntities().size());
        for (Map.Entry<BlockPos, NbtCompound> e : snapshot.blockEntities().entrySet()) {
            beEntries.add(new BlockEntityEntry(BotiRelativePosCodec.pack(e.getKey()), e.getValue().copy()));
        }
        List<EntityEntry> entityEntries = new ArrayList<>(snapshot.entities().size());
        for (BotiEntitySample sample : snapshot.entities()) {
            entityEntries.add(new EntityEntry(
                    sample.relX(),
                    sample.relY(),
                    sample.relZ(),
                    sample.yaw(),
                    sample.pitch(),
                    sample.nbt().copy()
            ));
        }
        return new SyncBotiInteriorS2CPayload(
                (byte) snapshot.formatVersion(),
                snapshot.tardisId(),
                snapshot.revision(),
                entries,
                beEntries,
                entityEntries
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

    public Map<BlockPos, NbtCompound> toBlockEntityMap() {
        Map<BlockPos, NbtCompound> map = new HashMap<>(blockEntities.size());
        for (BlockEntityEntry entry : blockEntities) {
            if (entry.nbt() != null) {
                map.put(BotiRelativePosCodec.unpack(entry.packedPos()), entry.nbt().copy());
            }
        }
        return map;
    }

    public List<BotiEntitySample> toEntityList() {
        List<BotiEntitySample> list = new ArrayList<>(entities.size());
        for (EntityEntry entry : entities) {
            if (entry.nbt() != null) {
                list.add(new BotiEntitySample(
                        entry.relX(),
                        entry.relY(),
                        entry.relZ(),
                        entry.yaw(),
                        entry.pitch(),
                        entry.nbt().copy()
                ));
            }
        }
        return list;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
