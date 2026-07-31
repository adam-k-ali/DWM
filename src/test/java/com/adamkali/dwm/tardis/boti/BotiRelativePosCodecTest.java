package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.network.SyncBotiInteriorS2CPayload;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotiRelativePosCodecTest {

    private static RegistryWrapper.WrapperLookup registries;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        registries = BuiltinRegistries.createWrapperLookup();
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
                new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.getDefaultState(),
                new BlockPos(5, 1, 5), DWMBlocks.TEAL_BIG_ROUNDEL_A.getDefaultState()
        );
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, 7, blocks);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(BotiInteriorSnapshot.FORMAT_VERSION_BLOCKS_AND_BES, payload.formatVersion());
        assertEquals(tardisId, payload.tardisId());
        assertEquals(7, payload.revision());
        assertEquals(2, payload.blocks().size());
        assertTrue(payload.blockEntities().isEmpty());

        Map<BlockPos, BlockState> decoded = payload.toBlockMap();
        assertEquals(blocks.get(new BlockPos(0, 0, 0)), decoded.get(new BlockPos(0, 0, 0)));
        assertEquals(blocks.get(new BlockPos(5, 1, 5)), decoded.get(new BlockPos(5, 1, 5)));
        assertFalse(decoded.containsValue(Blocks.AIR.getDefaultState()));
    }

    @Test
    void snapshotPayload_RoundTripsBlockEntityNbt() {
        UUID tardisId = UUID.randomUUID();
        BlockPos chestPos = new BlockPos(3, 1, 4);
        BlockState chestState = Blocks.CHEST.getDefaultState();
        ChestBlockEntity chest = new ChestBlockEntity(chestPos, chestState);
        NbtCompound nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);

        assertTrue(nbt.contains("id"));

        Map<BlockPos, BlockState> blocks = Map.of(chestPos, chestState);
        Map<BlockPos, NbtCompound> blockEntities = Map.of(chestPos, nbt);
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, 2, blocks, blockEntities);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(1, payload.blockEntities().size());
        Map<BlockPos, NbtCompound> decoded = payload.toBlockEntityMap();
        assertEquals(1, decoded.size());
        assertEquals("minecraft:chest", decoded.get(chestPos).getString("id"));

        BlockEntity rebuilt = BlockEntity.createFromNbt(chestPos, chestState, decoded.get(chestPos), registries);
        assertNotNull(rebuilt);
        assertInstanceOf(ChestBlockEntity.class, rebuilt);
    }

    @Test
    void captureSyncNbt_IncludesTypeIdForChest() {
        BlockPos pos = new BlockPos(1, 1, 1);
        ChestBlockEntity chest = new ChestBlockEntity(pos, Blocks.CHEST.getDefaultState());
        NbtCompound nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);
        assertEquals("minecraft:chest", nbt.getString("id"));
    }

    @Test
    void isBotiVisible_ExcludesInteriorDoorEvenWithBlockEntity() {
        assertFalse(BotiInteriorSampler.isBotiVisible(DWMBlocks.TARDIS_INTERIOR_DOOR.getDefaultState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(Blocks.CHEST.getDefaultState()));
    }
}
