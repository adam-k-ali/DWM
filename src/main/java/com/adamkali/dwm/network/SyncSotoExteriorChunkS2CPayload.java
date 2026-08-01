package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
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
 * S2C sparse chunk column for the Phase 1 ghost exterior store.
 * Block positions are footprint-relative (same space as snapshot blocks).
 */
public record SyncSotoExteriorChunkS2CPayload(
        UUID tardisId,
        int chunkX,
        int chunkZ,
        int footprintOriginX,
        int footprintOriginY,
        int footprintOriginZ,
        List<BlockEntry> blocks,
        List<BlockEntityEntry> blockEntities
) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorChunkS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_CHUNK_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorChunkS2CPayload> CODEC =
            PacketCodec.of(SyncSotoExteriorChunkS2CPayload::encode, SyncSotoExteriorChunkS2CPayload::decode);

    public record BlockEntry(int relX, int relY, int relZ, int stateId) {
    }

    public record BlockEntityEntry(int relX, int relY, int relZ, NbtCompound nbt) {
    }

    private static void encode(SyncSotoExteriorChunkS2CPayload payload, RegistryByteBuf buf) {
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        PacketCodecs.VAR_INT.encode(buf, payload.chunkX);
        PacketCodecs.VAR_INT.encode(buf, payload.chunkZ);
        PacketCodecs.VAR_INT.encode(buf, payload.footprintOriginX);
        PacketCodecs.VAR_INT.encode(buf, payload.footprintOriginY);
        PacketCodecs.VAR_INT.encode(buf, payload.footprintOriginZ);
        PacketCodecs.VAR_INT.encode(buf, payload.blocks.size());
        for (BlockEntry entry : payload.blocks) {
            PacketCodecs.VAR_INT.encode(buf, entry.relX());
            PacketCodecs.VAR_INT.encode(buf, entry.relY());
            PacketCodecs.VAR_INT.encode(buf, entry.relZ());
            PacketCodecs.VAR_INT.encode(buf, entry.stateId());
        }
        PacketCodecs.VAR_INT.encode(buf, payload.blockEntities.size());
        for (BlockEntityEntry entry : payload.blockEntities) {
            PacketCodecs.VAR_INT.encode(buf, entry.relX());
            PacketCodecs.VAR_INT.encode(buf, entry.relY());
            PacketCodecs.VAR_INT.encode(buf, entry.relZ());
            PacketCodecs.NBT_COMPOUND.encode(buf, entry.nbt());
        }
    }

    private static SyncSotoExteriorChunkS2CPayload decode(RegistryByteBuf buf) {
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int chunkX = PacketCodecs.VAR_INT.decode(buf);
        int chunkZ = PacketCodecs.VAR_INT.decode(buf);
        int originX = PacketCodecs.VAR_INT.decode(buf);
        int originY = PacketCodecs.VAR_INT.decode(buf);
        int originZ = PacketCodecs.VAR_INT.decode(buf);
        int blockCount = PacketCodecs.VAR_INT.decode(buf);
        List<BlockEntry> blocks = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            blocks.add(new BlockEntry(
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf)
            ));
        }
        int beCount = PacketCodecs.VAR_INT.decode(buf);
        List<BlockEntityEntry> blockEntities = new ArrayList<>(beCount);
        for (int i = 0; i < beCount; i++) {
            blockEntities.add(new BlockEntityEntry(
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.VAR_INT.decode(buf),
                    PacketCodecs.NBT_COMPOUND.decode(buf)
            ));
        }
        return new SyncSotoExteriorChunkS2CPayload(
                tardisId, chunkX, chunkZ, originX, originY, originZ, blocks, blockEntities
        );
    }

    public static SyncSotoExteriorChunkS2CPayload fromSample(
            UUID tardisId,
            BlockPos footprintOrigin,
            SotoExteriorSampler.StreamChunkSample sample
    ) {
        List<BlockEntry> blocks = new ArrayList<>(sample.blocks().size());
        for (Map.Entry<BlockPos, BlockState> entry : sample.blocks().entrySet()) {
            BlockPos world = entry.getKey();
            blocks.add(new BlockEntry(
                    world.getX() - footprintOrigin.getX(),
                    world.getY() - footprintOrigin.getY(),
                    world.getZ() - footprintOrigin.getZ(),
                    BotiRelativePosCodec.stateId(entry.getValue())
            ));
        }
        List<BlockEntityEntry> bes = new ArrayList<>(sample.blockEntities().size());
        for (Map.Entry<BlockPos, NbtCompound> entry : sample.blockEntities().entrySet()) {
            BlockPos world = entry.getKey();
            bes.add(new BlockEntityEntry(
                    world.getX() - footprintOrigin.getX(),
                    world.getY() - footprintOrigin.getY(),
                    world.getZ() - footprintOrigin.getZ(),
                    entry.getValue()
            ));
        }
        return new SyncSotoExteriorChunkS2CPayload(
                tardisId,
                sample.chunkX(),
                sample.chunkZ(),
                footprintOrigin.getX(),
                footprintOrigin.getY(),
                footprintOrigin.getZ(),
                blocks,
                bes
        );
    }

    public Map<BlockPos, BlockState> toBlockMap() {
        Map<BlockPos, BlockState> map = new HashMap<>(blocks.size());
        for (BlockEntry entry : blocks) {
            map.put(new BlockPos(entry.relX(), entry.relY(), entry.relZ()), BotiRelativePosCodec.stateFromId(entry.stateId()));
        }
        return map;
    }

    public Map<BlockPos, NbtCompound> toBlockEntityMap() {
        Map<BlockPos, NbtCompound> map = new HashMap<>(blockEntities.size());
        for (BlockEntityEntry entry : blockEntities) {
            map.put(new BlockPos(entry.relX(), entry.relY(), entry.relZ()), entry.nbt().copy());
        }
        return map;
    }

    public BlockPos footprintOrigin() {
        return new BlockPos(footprintOriginX, footprintOriginY, footprintOriginZ);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
