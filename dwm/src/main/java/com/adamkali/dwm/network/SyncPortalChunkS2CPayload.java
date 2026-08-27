package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.portal.PortalLightData;
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
        List<BlockEntityEntry> blockEntities,
        PortalLightData lightData
) implements CustomPacketPayload {
    private static final int MAX_LIGHT_BYTES = 16 * 384 * 16;

    public static final CustomPacketPayload.Type<SyncPortalChunkS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_CHUNK_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalChunkS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalChunkS2CPayload::encode, SyncPortalChunkS2CPayload::decode);

    public record BlockEntry(int relX, int relY, int relZ, int stateId) {
    }

    public record BlockEntityEntry(int relX, int relY, int relZ, CompoundTag nbt) {
    }

    public SyncPortalChunkS2CPayload(
            PortalStreamKind kind,
            UUID tardisId,
            int chunkX,
            int chunkZ,
            int footprintOriginX,
            int footprintOriginY,
            int footprintOriginZ,
            List<BlockEntry> blocks,
            List<BlockEntityEntry> blockEntities
    ) {
        this(
                kind, tardisId, chunkX, chunkZ,
                footprintOriginX, footprintOriginY, footprintOriginZ,
                blocks, blockEntities, PortalLightData.EMPTY
        );
    }

    public SyncPortalChunkS2CPayload {
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
        blockEntities = blockEntities == null ? List.of() : List.copyOf(blockEntities);
        lightData = lightData == null ? PortalLightData.EMPTY : lightData;
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
        PortalLightData light = payload.lightData;
        ByteBufCodecs.VAR_INT.encode(buf, light.min().getX());
        ByteBufCodecs.VAR_INT.encode(buf, light.min().getY());
        ByteBufCodecs.VAR_INT.encode(buf, light.min().getZ());
        ByteBufCodecs.VAR_INT.encode(buf, light.sizeX());
        ByteBufCodecs.VAR_INT.encode(buf, light.sizeY());
        ByteBufCodecs.VAR_INT.encode(buf, light.sizeZ());
        buf.writeByteArray(light.packedCopy());
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
        BlockPos lightMin = new BlockPos(
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf)
        );
        int lightSizeX = ByteBufCodecs.VAR_INT.decode(buf);
        int lightSizeY = ByteBufCodecs.VAR_INT.decode(buf);
        int lightSizeZ = ByteBufCodecs.VAR_INT.decode(buf);
        PortalLightData lightData = new PortalLightData(
                lightMin, lightSizeX, lightSizeY, lightSizeZ, buf.readByteArray(MAX_LIGHT_BYTES)
        );
        return new SyncPortalChunkS2CPayload(
                kind, tardisId, chunkX, chunkZ, originX, originY, originZ, blocks, blockEntities, lightData
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
        PortalLightData relativeLight = sample.lightData().translated(new BlockPos(
                -footprintOrigin.getX(),
                -footprintOrigin.getY(),
                -footprintOrigin.getZ()
        ));
        // #region agent log
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"),
                    "{\"hypothesisId\":\"B\",\"location\":\"SyncPortalChunkS2CPayload.fromSample\",\"message\":\"translated portal light for wire\",\"data\":{\"kind\":\"" + kind + "\",\"chunkX\":" + sample.chunkX() + ",\"chunkZ\":" + sample.chunkZ() + ",\"originX\":" + footprintOrigin.getX() + ",\"originY\":" + footprintOrigin.getY() + ",\"originZ\":" + footprintOrigin.getZ() + ",\"worldMinX\":" + sample.lightData().min().getX() + ",\"worldMinY\":" + sample.lightData().min().getY() + ",\"worldMinZ\":" + sample.lightData().min().getZ() + ",\"relativeMinX\":" + relativeLight.min().getX() + ",\"relativeMinY\":" + relativeLight.min().getY() + ",\"relativeMinZ\":" + relativeLight.min().getZ() + ",\"bytes\":" + relativeLight.packedCopy().length + "},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException ignored) {
        }
        // #endregion
        return new SyncPortalChunkS2CPayload(
                kind,
                tardisId,
                sample.chunkX(),
                sample.chunkZ(),
                footprintOrigin.getX(),
                footprintOrigin.getY(),
                footprintOrigin.getZ(),
                blocks,
                bes,
                relativeLight
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
