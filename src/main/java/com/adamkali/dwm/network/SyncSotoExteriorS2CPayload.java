package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * S2C full SOTO exterior footprint snapshot.
 * formatVersion 4 = blocks + BE NBT + entities + shell + atmosphere.
 */
public record SyncSotoExteriorS2CPayload(
        byte formatVersion,
        UUID tardisId,
        int revision,
        List<SyncBotiInteriorS2CPayload.Entry> blocks,
        List<SyncBotiInteriorS2CPayload.BlockEntityEntry> blockEntities,
        List<SyncBotiInteriorS2CPayload.EntityEntry> entities,
        Identifier variantId,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation,
        SotoAtmosphere atmosphere
) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorS2CPayload> CODEC =
            PacketCodec.of(SyncSotoExteriorS2CPayload::encode, SyncSotoExteriorS2CPayload::decode);

    private static void encode(SyncSotoExteriorS2CPayload payload, RegistryByteBuf buf) {
        buf.writeByte(payload.formatVersion);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        PacketCodecs.VAR_INT.encode(buf, payload.revision);
        PacketCodecs.VAR_INT.encode(buf, payload.blocks.size());
        for (SyncBotiInteriorS2CPayload.Entry entry : payload.blocks) {
            SyncBotiInteriorS2CPayload.ENTRY_CODEC.encode(buf, entry);
        }
        PacketCodecs.VAR_INT.encode(buf, payload.blockEntities.size());
        for (SyncBotiInteriorS2CPayload.BlockEntityEntry entry : payload.blockEntities) {
            SyncBotiInteriorS2CPayload.BLOCK_ENTITY_ENTRY_CODEC.encode(buf, entry);
        }
        PacketCodecs.VAR_INT.encode(buf, payload.entities.size());
        for (SyncBotiInteriorS2CPayload.EntityEntry entry : payload.entities) {
            SyncBotiInteriorS2CPayload.ENTITY_ENTRY_CODEC.encode(buf, entry);
        }
        Identifier.PACKET_CODEC.encode(buf, payload.variantId);
        buf.writeFloat(payload.doorSwing);
        buf.writeBoolean(payload.isOpen);
        PacketCodecs.VAR_INT.encode(buf, payload.exteriorRotation);
        encodeAtmosphere(payload.atmosphere(), buf);
    }

    private static SyncSotoExteriorS2CPayload decode(RegistryByteBuf buf) {
        byte formatVersion = buf.readByte();
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int revision = PacketCodecs.VAR_INT.decode(buf);
        int blockCount = PacketCodecs.VAR_INT.decode(buf);
        List<SyncBotiInteriorS2CPayload.Entry> blocks = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            blocks.add(SyncBotiInteriorS2CPayload.ENTRY_CODEC.decode(buf));
        }
        int beCount = PacketCodecs.VAR_INT.decode(buf);
        List<SyncBotiInteriorS2CPayload.BlockEntityEntry> blockEntities = new ArrayList<>(beCount);
        for (int i = 0; i < beCount; i++) {
            blockEntities.add(SyncBotiInteriorS2CPayload.BLOCK_ENTITY_ENTRY_CODEC.decode(buf));
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
        SotoAtmosphere atmosphere = formatVersion >= SotoExteriorSnapshot.FORMAT_VERSION_ATMOSPHERE
                ? decodeAtmosphere(buf)
                : SotoAtmosphere.DEFAULT;
        return new SyncSotoExteriorS2CPayload(
                formatVersion,
                tardisId,
                revision,
                blocks,
                blockEntities,
                entities,
                variantId,
                doorSwing,
                isOpen,
                exteriorRotation,
                atmosphere
        );
    }

    private static void encodeAtmosphere(SotoAtmosphere atmosphere, RegistryByteBuf buf) {
        SotoAtmosphere value = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        Identifier.PACKET_CODEC.encode(buf, value.dimensionEffectsId());
        buf.writeLong(value.timeOfDay());
        buf.writeFloat(value.rainGradient());
        buf.writeFloat(value.thunderGradient());
        buf.writeInt(value.biomeSkyColor());
        buf.writeInt(value.biomeFogColor());
    }

    private static SotoAtmosphere decodeAtmosphere(RegistryByteBuf buf) {
        Identifier effectsId = Identifier.PACKET_CODEC.decode(buf);
        if (effectsId == null) {
            effectsId = DimensionTypes.OVERWORLD_ID;
        }
        return new SotoAtmosphere(
                effectsId,
                buf.readLong(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static SyncSotoExteriorS2CPayload fromSnapshot(SotoExteriorSnapshot snapshot) {
        List<SyncBotiInteriorS2CPayload.Entry> entries = new ArrayList<>(snapshot.blocks().size());
        for (Map.Entry<BlockPos, BlockState> e : snapshot.blocks().entrySet()) {
            entries.add(new SyncBotiInteriorS2CPayload.Entry(
                    BotiRelativePosCodec.pack(e.getKey()),
                    BotiRelativePosCodec.stateId(e.getValue())
            ));
        }
        List<SyncBotiInteriorS2CPayload.BlockEntityEntry> beEntries =
                new ArrayList<>(snapshot.blockEntities().size());
        for (Map.Entry<BlockPos, NbtCompound> e : snapshot.blockEntities().entrySet()) {
            beEntries.add(new SyncBotiInteriorS2CPayload.BlockEntityEntry(
                    BotiRelativePosCodec.pack(e.getKey()),
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
                entries,
                beEntries,
                entityEntries,
                snapshot.variant().getId(),
                snapshot.doorSwing(),
                snapshot.isOpen(),
                snapshot.exteriorRotation(),
                snapshot.atmosphere()
        );
    }

    public Map<BlockPos, BlockState> toBlockMap() {
        Map<BlockPos, BlockState> map = new HashMap<>(blocks.size());
        for (SyncBotiInteriorS2CPayload.Entry entry : blocks) {
            BlockState state = BotiRelativePosCodec.stateFromId(entry.stateId());
            if (state != null && !state.isAir()) {
                map.put(BotiRelativePosCodec.unpack(entry.packedPos()), state);
            }
        }
        return map;
    }

    public Map<BlockPos, NbtCompound> toBlockEntityMap() {
        Map<BlockPos, NbtCompound> map = new HashMap<>(blockEntities.size());
        for (SyncBotiInteriorS2CPayload.BlockEntityEntry entry : blockEntities) {
            if (entry.nbt() != null) {
                map.put(BotiRelativePosCodec.unpack(entry.packedPos()), entry.nbt().copy());
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
