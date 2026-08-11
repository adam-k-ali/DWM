package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntitySpawnS2CPayload;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
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
    void portalChunkPayload_RoundTripsBlockMap() {
        UUID tardisId = UUID.randomUUID();
        BlockPos footprintOrigin = BlockPos.ZERO;
        BlockPos worldA = new BlockPos(0, 0, 0);
        BlockPos worldB = new BlockPos(5, 1, 5);
        PortalStreamSample sample = new PortalStreamSample(
                0,
                0,
                Map.of(
                        worldA, DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState(),
                        worldB, DWMBlocks.TEAL_BIG_ROUNDEL_A.defaultBlockState()
                ),
                Map.of()
        );
        SyncPortalChunkS2CPayload payload =
                SyncPortalChunkS2CPayload.fromSample(PortalStreamKind.BOTI, tardisId, footprintOrigin, sample);

        assertEquals(PortalStreamKind.BOTI, payload.kind());
        assertEquals(tardisId, payload.tardisId());
        assertEquals(2, payload.blocks().size());
        assertTrue(payload.blockEntities().isEmpty());

        Map<BlockPos, BlockState> decoded = payload.toBlockMap();
        assertEquals(DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState(), decoded.get(new BlockPos(0, 0, 0)));
        assertEquals(DWMBlocks.TEAL_BIG_ROUNDEL_A.defaultBlockState(), decoded.get(new BlockPos(5, 1, 5)));
        assertFalse(decoded.containsValue(Blocks.AIR.defaultBlockState()));
    }

    @Test
    void portalChunkPayload_RoundTripsBlockEntityNbt() {
        UUID tardisId = UUID.randomUUID();
        BlockPos chestPos = new BlockPos(3, 1, 4);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        ChestBlockEntity chest = new ChestBlockEntity(chestPos, chestState);
        CompoundTag nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);

        assertTrue(nbt.contains("id"));

        PortalStreamSample sample = new PortalStreamSample(
                0, 0, Map.of(chestPos, chestState), Map.of(chestPos, nbt)
        );
        SyncPortalChunkS2CPayload payload =
                SyncPortalChunkS2CPayload.fromSample(PortalStreamKind.SOTO, tardisId, BlockPos.ZERO, sample);

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
    void portalEntitySpawn_RetainsPoseAndNbt() {
        UUID tardisId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        CompoundTag entityNbt = new CompoundTag();
        entityNbt.putString("id", "minecraft:armor_stand");
        entityNbt.store(BotiEntitySample.BOTI_PROFILE_ID, UUIDUtil.CODEC, UUID.randomUUID());
        BotiInteriorSampler.writeRelativePos(entityNbt, 5.5f, 1.0f, 2.25f);

        SyncPortalEntitySpawnS2CPayload spawn = new SyncPortalEntitySpawnS2CPayload(
                PortalStreamKind.BOTI,
                tardisId,
                entityId,
                Identifier.fromNamespaceAndPath("minecraft", "armor_stand"),
                5.5f,
                1.0f,
                2.25f,
                90f,
                10f,
                90f,
                90f,
                0.0,
                0.0,
                0.0,
                entityNbt
        );

        assertEquals(PortalStreamKind.BOTI, spawn.kind());
        assertEquals(5.5f, spawn.relX(), 0.0001f);
        assertEquals(1.0f, spawn.relY(), 0.0001f);
        assertEquals(2.25f, spawn.relZ(), 0.0001f);
        assertEquals(90f, spawn.yaw(), 0.0001f);
        assertEquals(10f, spawn.pitch(), 0.0001f);
        assertEquals("minecraft:armor_stand", spawn.nbt().getString("id").orElseThrow());
    }

    @Test
    void isBotiVisible_ExcludesInteriorDoorEvenWithBlockEntity() {
        assertFalse(BotiInteriorSampler.isBotiVisible(DWMBlocks.TARDIS_INTERIOR_DOOR.defaultBlockState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(Blocks.CHEST.defaultBlockState()));
    }
}
