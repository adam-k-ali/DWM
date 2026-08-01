package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.adamkali.dwm.tardis.soto.SotoExteriorSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * S2C SOTO exterior snapshot. formatVersion 4 = signed relative positions + radiusChunks + shell.
 */
public record SyncSotoExteriorS2CPayload(
        byte formatVersion,
        UUID tardisId,
        int revision,
        int radiusChunks,
        List<RelativeBlockEntry> blocks,
        List<RelativeBlockEntityEntry> blockEntities,
        List<SyncBotiInteriorS2CPayload.EntityEntry> entities,
        Identifier variantId,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation
) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorS2CPayload> CODEC =
            PacketCodec.of(SyncSotoExteriorS2CPayload::encode, SyncSotoExteriorS2CPayload::decode);

    public record RelativeBlockEntry(int x, int y, int z, int stateId) {
    }

    public record RelativeBlockEntityEntry(int x, int y, int z, NbtCompound nbt) {
    }

    private static void encode(SyncSotoExteriorS2CPayload payload, RegistryByteBuf buf) {
        buf.writeByte(payload.formatVersion);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        PacketCodecs.VAR_INT.encode(buf, payload.revision);
        PacketCodecs.VAR_INT.encode(buf, payload.radiusChunks);
        PacketCodecs.VAR_INT.encode(buf, payload.blocks.size());
        for (RelativeBlockEntry entry : payload.blocks) {
            PacketCodecs.VAR_INT.encode(buf, entry.x());
            PacketCodecs.VAR_INT.encode(buf, entry.y());
            PacketCodecs.VAR_INT.encode(buf, entry.z());
            PacketCodecs.VAR_INT.encode(buf, entry.stateId());
        }
        PacketCodecs.VAR_INT.encode(buf, payload.blockEntities.size());
        for (RelativeBlockEntityEntry entry : payload.blockEntities) {
            PacketCodecs.VAR_INT.encode(buf, entry.x());
            PacketCodecs.VAR_INT.encode(buf, entry.y());
            PacketCodecs.VAR_INT.encode(buf, entry.z());
            PacketCodecs.NBT_COMPOUND.encode(buf, entry.nbt());
        }
        PacketCodecs.VAR_INT.encode(buf, payload.entities.size());
        for (SyncBotiInteriorS2CPayload.EntityEntry entry : payload.entities) {
            SyncBotiInteriorS2CPayload.ENTITY_ENTRY_CODEC.encode(buf, entry);
        }
        Identifier.PACKET_CODEC.encode(buf, payload.variantId);
        buf.writeFloat(payload.doorSwing);
        buf.writeBoolean(payload.isOpen);
        PacketCodecs.VAR_INT.encode(buf, payload.exteriorRotation);
    }

    private static SyncSotoExteriorS2CPayload decode(RegistryByteBuf buf) {
        byte formatVersion = buf.readByte();
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int revision = PacketCodecs.VAR_INT.decode(buf);
        int radiusChunks = PacketCodecs.VAR_INT.decode(buf);
        int blockCount = PacketCodecs.VAR_INT.decode(buf);
        List<RelativeBlockEntry> blocks = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            blocks.add(new RelativeBlockEntry(
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf)
            ));
        }
        int beCount = PacketCodecs.VAR_INT.decode(buf);
        List<RelativeBlockEntityEntry> blockEntities = new ArrayList<>(beCount);
        for (int i = 0; i < beCount; i++) {
            blockEntities.add(new RelativeBlockEntityEntry(
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.NBT_COMPOUND.decode(buf)
            ));
        }
        int entityCount = PacketCodecs.VAR_INT.decode(buf);
        List<SyncBotiInteriorS2CPayload.EntityEntry> entities = new ArrayList<>(entityCount);
        for (int i = 0; i < entityCount; i++) {
            entities.add(SyncBotiInteriorS2CPayload.ENTITY_ENTRY_CODEC.decode(buf));
        }
        Identifier variantId = Identifier.PACKET_CODEC.decode(buf);
        float doorSwing = buf.readFloat();
        boolean isOpen = buf.readBoolean();
        int exteriorRotation = PacketCodecs.VAR_INT.decode(buf);
        return new SyncSotoExteriorS2CPayload(
                formatVersion,
                tardisId,
                revision,
                SotoExteriorSampler.clampRadiusChunks(radiusChunks),
                blocks,
                blockEntities,
                entities,
                variantId,
                doorSwing,
                isOpen,
                exteriorRotation
        );
    }

    public static SyncSotoExteriorS2CPayload fromSnapshot(SotoExteriorSnapshot snapshot) {
        List<RelativeBlockEntry> entries = new ArrayList<>(snapshot.blocks().size());
        for (Map.Entry<BlockPos, BlockState> e : snapshot.blocks().entrySet()) {
            BlockPos pos = e.getKey();
            entries.add(new RelativeBlockEntry(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    BotiRelativePosCodec.stateId(e.getValue())
            ));
        }
        List<RelativeBlockEntityEntry> beEntries = new ArrayList<>(snapshot.blockEntities().size());
        for (Map.Entry<BlockPos, NbtCompound> e : snapshot.blockEntities().entrySet()) {
            BlockPos pos = e.getKey();
            beEntries.add(new RelativeBlockEntityEntry(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    e.getValue().copy()
            ));
        }
        List<SyncBotiInteriorS2CPayload.EntityEntry> entityEntries =
                new ArrayList<>(snapshot.entities().size());
        for (BotiEntitySample sample : snapshot.entities()) {
            entityEntries.add(new SyncBotiInteriorS2CPayload.EntityEntry(
                    sample.relX(),
                    sample.relY(),
                    sample.relZ(),
                    sample.yaw(),
                    sample.pitch(),
                    sample.nbt().copy()
            ));
        }
        return new SyncSotoExteriorS2CPayload(
                (byte) snapshot.formatVersion(),
                snapshot.tardisId(),
                snapshot.revision(),
                snapshot.radiusChunks(),
                entries,
                beEntries,
                entityEntries,
                snapshot.variant().getId(),
                snapshot.doorSwing(),
                snapshot.isOpen(),
                snapshot.exteriorRotation()
        );
    }

    public Map<BlockPos, BlockState> toBlockMap() {
        Map<BlockPos, BlockState> map = new HashMap<>(blocks.size());
        for (RelativeBlockEntry entry : blocks) {
            BlockState state = BotiRelativePosCodec.stateFromId(entry.stateId());
            if (state != null && !state.isAir()) {
                map.put(new BlockPos(entry.x(), entry.y(), entry.z()), state);
            }
        }
        return map;
    }

    public Map<BlockPos, NbtCompound> toBlockEntityMap() {
        Map<BlockPos, NbtCompound> map = new HashMap<>(blockEntities.size());
        for (RelativeBlockEntityEntry entry : blockEntities) {
            if (entry.nbt() != null) {
                map.put(new BlockPos(entry.x(), entry.y(), entry.z()), entry.nbt().copy());
            }
        }
        return map;
    }

    public List<BotiEntitySample> toEntityList() {
        List<BotiEntitySample> list = new ArrayList<>(entities.size());
        for (SyncBotiInteriorS2CPayload.EntityEntry entry : entities) {
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

    public TardisChameleonVariant variant() {
        try {
            return TardisChameleonVariant.fromId(variantId);
        } catch (IllegalArgumentException e) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
