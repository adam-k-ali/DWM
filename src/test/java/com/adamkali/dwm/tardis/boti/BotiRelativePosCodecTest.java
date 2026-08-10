package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.network.SyncBotiInteriorS2CPayload;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;

class BotiRelativePosCodecTest {

    private static HolderLookup.Provider registries;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        registries = VanillaRegistries.createLookup();
    }

    @Test
    void packUnpack_RoundTripsFootprintCoords() {
        for (int x = 0; x < BotiInteriorSampler.SIZE_X; x++) {
            for (int y = 0; y < BotiInteriorSampler.SIZE_Y; y++) {
                for (int z = 0; z < BotiInteriorSampler.SIZE_Z; z++) {
                    BlockPos original = new BlockPos(x, y, z);
                    assertEquals(original, BotiRelativePosCodec.unpack(BotiRelativePosCodec.pack(original)));
                }
            }
        }
    }

    @Test
    void snapshotPayload_RoundTripsBlockMap() {
        UUID tardisId = UUID.randomUUID();
        Map<BlockPos, BlockState> blocks = Map.of(
                new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState(),
                new BlockPos(5, 1, 5), DWMBlocks.TEAL_BIG_ROUNDEL_A.defaultBlockState()
        );
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, 7, blocks);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(BotiInteriorSnapshot.FORMAT_VERSION_BLOCKS_BES_AND_ENTITIES, payload.formatVersion());
        assertEquals(tardisId, payload.tardisId());
        assertEquals(7, payload.revision());
        assertEquals(2, payload.blocks().size());
        assertTrue(payload.blockEntities().isEmpty());
        assertTrue(payload.entities().isEmpty());

        Map<BlockPos, BlockState> decoded = payload.toBlockMap();
        assertEquals(blocks.get(new BlockPos(0, 0, 0)), decoded.get(new BlockPos(0, 0, 0)));
        assertEquals(blocks.get(new BlockPos(5, 1, 5)), decoded.get(new BlockPos(5, 1, 5)));
        assertFalse(decoded.containsValue(Blocks.AIR.defaultBlockState()));
    }

    @Test
    void snapshotPayload_RoundTripsBlockEntityNbt() {
        UUID tardisId = UUID.randomUUID();
        BlockPos chestPos = new BlockPos(3, 1, 4);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        ChestBlockEntity chest = new ChestBlockEntity(chestPos, chestState);
        CompoundTag nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);

        assertTrue(nbt.contains("id"));

        Map<BlockPos, BlockState> blocks = Map.of(chestPos, chestState);
        Map<BlockPos, CompoundTag> blockEntities = Map.of(chestPos, nbt);
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, 2, blocks, blockEntities);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(1, payload.blockEntities().size());
        Map<BlockPos, CompoundTag> decoded = payload.toBlockEntityMap();
        assertEquals(1, decoded.size());
        assertEquals("minecraft:chest", decoded.get(chestPos).getString("id").orElseThrow());

        BlockEntity rebuilt = BlockEntity.loadStatic(chestPos, chestState, decoded.get(chestPos), registries);
        assertNotNull(rebuilt);
        assertInstanceOf(ChestBlockEntity.class, rebuilt);
    }

    @Test
    void captureSyncNbt_IncludesTypeIdForChest() {
        BlockPos pos = new BlockPos(1, 1, 1);
        ChestBlockEntity chest = new ChestBlockEntity(pos, Blocks.CHEST.defaultBlockState());
        CompoundTag nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);
        assertEquals("minecraft:chest", nbt.getString("id").orElseThrow());
    }

    @Test
    void snapshotPayload_RoundTripsEntitySamples() {
        UUID tardisId = UUID.randomUUID();
        Map<BlockPos, BlockState> blocks = Map.of(
                new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()
        );
        CompoundTag entityNbt = new CompoundTag();
        entityNbt.putString("id", "minecraft:armor_stand");
        entityNbt.store(BotiEntitySample.BOTI_PROFILE_ID, UUIDUtil.CODEC, UUID.randomUUID()); // ignored for non-players
        BotiInteriorSampler.writeRelativePos(entityNbt, 5.5f, 1.0f, 2.25f);

        BotiEntitySample sample = new BotiEntitySample(5.5f, 1.0f, 2.25f, 90f, 10f, entityNbt);
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(
                tardisId,
                3,
                blocks,
                Map.of(),
                java.util.List.of(sample)
        );
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(BotiInteriorSnapshot.FORMAT_VERSION_BLOCKS_BES_AND_ENTITIES, payload.formatVersion());
        assertEquals(1, payload.entities().size());

        java.util.List<BotiEntitySample> decoded = payload.toEntityList();
        assertEquals(1, decoded.size());
        BotiEntitySample roundTrip = decoded.getFirst();
        assertEquals(5.5f, roundTrip.relX(), 0.0001f);
        assertEquals(1.0f, roundTrip.relY(), 0.0001f);
        assertEquals(2.25f, roundTrip.relZ(), 0.0001f);
        assertEquals(90f, roundTrip.yaw(), 0.0001f);
        assertEquals(10f, roundTrip.pitch(), 0.0001f);
        assertEquals("minecraft:armor_stand", roundTrip.nbt().getString("id").orElseThrow());
    }

    @Test
    void isBotiVisible_ExcludesInteriorDoorEvenWithBlockEntity() {
        assertFalse(BotiInteriorSampler.isBotiVisible(DWMBlocks.TARDIS_INTERIOR_DOOR.defaultBlockState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(Blocks.CHEST.defaultBlockState()));
    }
}
