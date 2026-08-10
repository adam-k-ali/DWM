package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiInteriorSnapshot;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.state.BlockState;

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
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncBotiInteriorS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_BOTI_INTERIOR_PACKET_ID);

    public record Entry(short packedPos, int stateId) {
    }

    public record BlockEntityEntry(short packedPos, CompoundTag nbt) {
    }

    public record EntityEntry(
            float relX,
            float relY,
            float relZ,
            float yaw,
            float pitch,
            CompoundTag nbt
    ) {
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, Entry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT, Entry::packedPos,
            ByteBufCodecs.VAR_INT, Entry::stateId,
            Entry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityEntry> BLOCK_ENTITY_ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT, BlockEntityEntry::packedPos,
            ByteBufCodecs.COMPOUND_TAG, BlockEntityEntry::nbt,
            BlockEntityEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityEntry> ENTITY_ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, EntityEntry::relX,
            ByteBufCodecs.FLOAT, EntityEntry::relY,
            ByteBufCodecs.FLOAT, EntityEntry::relZ,
            ByteBufCodecs.FLOAT, EntityEntry::yaw,
            ByteBufCodecs.FLOAT, EntityEntry::pitch,
            ByteBufCodecs.COMPOUND_TAG, EntityEntry::nbt,
            EntityEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBotiInteriorS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, SyncBotiInteriorS2CPayload::formatVersion,
            DWMPacketCodecs.UUID_PACKET_CODEC, SyncBotiInteriorS2CPayload::tardisId,
            ByteBufCodecs.VAR_INT, SyncBotiInteriorS2CPayload::revision,
            ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncBotiInteriorS2CPayload::blocks,
            BLOCK_ENTITY_ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncBotiInteriorS2CPayload::blockEntities,
            ENTITY_ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncBotiInteriorS2CPayload::entities,
            SyncBotiInteriorS2CPayload::new
    );

    public static SyncBotiInteriorS2CPayload fromSnapshot(BotiInteriorSnapshot snapshot) {
        List<Entry> entries = new ArrayList<>(snapshot.blocks().size());
        for (Map.Entry<BlockPos, BlockState> e : snapshot.blocks().entrySet()) {
            entries.add(new Entry(BotiRelativePosCodec.pack(e.getKey()), BotiRelativePosCodec.stateId(e.getValue())));
        }
        List<BlockEntityEntry> beEntries = new ArrayList<>(snapshot.blockEntities().size());
        for (Map.Entry<BlockPos, CompoundTag> e : snapshot.blockEntities().entrySet()) {
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

    public Map<BlockPos, CompoundTag> toBlockEntityMap() {
        Map<BlockPos, CompoundTag> map = new HashMap<>(blockEntities.size());
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
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
