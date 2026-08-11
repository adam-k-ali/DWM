package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
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
 * S2C sparse chunk column for the portal ghost store.
 * Block positions are footprint-relative.
 */
public record SyncPortalChunkS2CPayload(
        PortalStreamKind kind,
        UUID tardisId,
        int chunkX,
        int chunkZ,
        int footprintOriginX,
        int footprintOriginY,
        int footprintOriginZ,
        List<BlockEntry> blocks,
        List<BlockEntityEntry> blockEntities
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPortalChunkS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_CHUNK_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalChunkS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalChunkS2CPayload::encode, SyncPortalChunkS2CPayload::decode);

    public record BlockEntry(int relX, int relY, int relZ, int stateId) {
    }

    public record BlockEntityEntry(int relX, int relY, int relZ, CompoundTag nbt) {
    }

    private static void encode(SyncPortalChunkS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.kind.toWire());
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        ByteBufCodecs.VAR_INT.encode(buf, payload.chunkX);
        ByteBufCodecs.VAR_INT.encode(buf, payload.chunkZ);
        ByteBufCodecs.VAR_INT.encode(buf, payload.footprintOriginX);
        ByteBufCodecs.VAR_INT.encode(buf, payload.footprintOriginY);
        ByteBufCodecs.VAR_INT.encode(buf, payload.footprintOriginZ);
        ByteBufCodecs.VAR_INT.encode(buf, payload.blocks.size());
        for (BlockEntry entry : payload.blocks) {
            ByteBufCodecs.VAR_INT.encode(buf, entry.relX());
            ByteBufCodecs.VAR_INT.encode(buf, entry.relY());
            ByteBufCodecs.VAR_INT.encode(buf, entry.relZ());
            ByteBufCodecs.VAR_INT.encode(buf, entry.stateId());
        }
        ByteBufCodecs.VAR_INT.encode(buf, payload.blockEntities.size());
        for (BlockEntityEntry entry : payload.blockEntities) {
            ByteBufCodecs.VAR_INT.encode(buf, entry.relX());
            ByteBufCodecs.VAR_INT.encode(buf, entry.relY());
            ByteBufCodecs.VAR_INT.encode(buf, entry.relZ());
            ByteBufCodecs.COMPOUND_TAG.encode(buf, entry.nbt());
        }
    }

    private static SyncPortalChunkS2CPayload decode(RegistryFriendlyByteBuf buf) {
        PortalStreamKind kind = PortalStreamKind.fromWire(buf.readByte());
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int chunkX = ByteBufCodecs.VAR_INT.decode(buf);
        int chunkZ = ByteBufCodecs.VAR_INT.decode(buf);
        int originX = ByteBufCodecs.VAR_INT.decode(buf);
        int originY = ByteBufCodecs.VAR_INT.decode(buf);
        int originZ = ByteBufCodecs.VAR_INT.decode(buf);
        int blockCount = ByteBufCodecs.VAR_INT.decode(buf);
        List<BlockEntry> blocks = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            blocks.add(new BlockEntry(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
            ));
        }
        int beCount = ByteBufCodecs.VAR_INT.decode(buf);
        List<BlockEntityEntry> blockEntities = new ArrayList<>(beCount);
        for (int i = 0; i < beCount; i++) {
            blockEntities.add(new BlockEntityEntry(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.COMPOUND_TAG.decode(buf)
            ));
        }
        return new SyncPortalChunkS2CPayload(
                kind, tardisId, chunkX, chunkZ, originX, originY, originZ, blocks, blockEntities
        );
    }

    public static SyncPortalChunkS2CPayload fromSample(
            PortalStreamKind kind,
            UUID tardisId,
            BlockPos footprintOrigin,
            PortalStreamSample sample
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
        for (Map.Entry<BlockPos, CompoundTag> entry : sample.blockEntities().entrySet()) {
            BlockPos world = entry.getKey();
            bes.add(new BlockEntityEntry(
                    world.getX() - footprintOrigin.getX(),
                    world.getY() - footprintOrigin.getY(),
                    world.getZ() - footprintOrigin.getZ(),
                    entry.getValue()
            ));
        }
        return new SyncPortalChunkS2CPayload(
                kind,
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

    public Map<BlockPos, CompoundTag> toBlockEntityMap() {
        Map<BlockPos, CompoundTag> map = new HashMap<>(blockEntities.size());
        for (BlockEntityEntry entry : blockEntities) {
            map.put(new BlockPos(entry.relX(), entry.relY(), entry.relZ()), entry.nbt().copy());
        }
        return map;
    }

    public BlockPos footprintOrigin() {
        return new BlockPos(footprintOriginX, footprintOriginY, footprintOriginZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
