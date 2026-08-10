package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;

class BotiInteriorMeshCacheTest {

    private static HolderLookup.Provider registries;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        registries = VanillaRegistries.createLookup();
    }

    @AfterEach
    void tearDown() {
        BotiInteriorMeshCache.invalidateAll();
    }

    @Test
    void getVisibleBlocks_FallsBackToBlueprintWithoutSnapshot() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> visible = BotiInteriorMeshCache.getVisibleBlocks(id);
        assertEquals(FirstDoctorConsoleRoomLayout.botiVisiblePlacements(), visible);
        assertFalse(BotiInteriorMeshCache.hasSnapshot(id));
        assertTrue(visible.containsKey(new BlockPos(5, 1, 5)));
        assertEquals(DWMBlocks.FIRST_DOCTOR_CONSOLE, visible.get(new BlockPos(5, 1, 5)).getBlock());
    }

    @Test
    void getBlockEntities_FallsBackToBlueprintConsoleWithoutSnapshot() {
        UUID id = UUID.randomUUID();
        List<BlockEntity> entities = BotiInteriorMeshCache.getBlockEntities(id);
        assertEquals(1, entities.size());
        BlockEntity be = entities.getFirst();
        assertInstanceOf(FirstDoctorConsoleBlockEntity.class, be);
        assertEquals(new BlockPos(5, 1, 5), be.getBlockPos());
        assertFalse(BotiInteriorMeshCache.hasSnapshot(id));
    }

    @Test
    void applySnapshot_WinsOverBlueprint() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> live = Map.of(
                new BlockPos(1, 1, 1), DWMBlocks.WHITE_ROUNDEL_A.defaultBlockState()
        );
        BotiInteriorMeshCache.applySnapshot(id, 1, live);

        assertTrue(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(live, BotiInteriorMeshCache.getVisibleBlocks(id));
    }

    @Test
    void applySnapshot_IgnoresOlderRevision() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> newer = Map.of(
                new BlockPos(2, 2, 2), DWMBlocks.TEAL_BIG_ROUNDEL_A.defaultBlockState()
        );
        Map<BlockPos, BlockState> older = Map.of(
                new BlockPos(3, 3, 3), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()
        );
        BotiInteriorMeshCache.applySnapshot(id, 5, newer);
        BotiInteriorMeshCache.applySnapshot(id, 4, older);

        assertEquals(newer, BotiInteriorMeshCache.getVisibleBlocks(id));
    }

    @Test
    void applySnapshot_IgnoresEmptyMap() {
        UUID id = UUID.randomUUID();
        BotiInteriorMeshCache.applySnapshot(id, 1, Map.of());
        assertFalse(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(FirstDoctorConsoleRoomLayout.botiVisiblePlacements(), BotiInteriorMeshCache.getVisibleBlocks(id));
    }

    @Test
    void applySnapshot_StoresBlockEntityNbt() {
        UUID id = UUID.randomUUID();
        BlockPos chestPos = new BlockPos(2, 1, 3);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        ChestBlockEntity chest = new ChestBlockEntity(chestPos, chestState);
        CompoundTag nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);

        BotiInteriorMeshCache.applySnapshot(
                id,
                1,
                Map.of(chestPos, chestState, new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()),
                Map.of(chestPos, nbt)
        );

        assertTrue(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(1, BotiInteriorMeshCache.getBlockEntityNbtCount(id));
        // Without a client world, synthetic BEs are not rebuilt yet.
        assertTrue(BotiInteriorMeshCache.getBlockEntities(id).isEmpty());
    }

    @Test
    void applySnapshot_OlderRevisionDoesNotReplaceBlockEntities() {
        UUID id = UUID.randomUUID();
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        CompoundTag newerNbt = BotiInteriorSampler.captureSyncNbt(new ChestBlockEntity(pos, chestState), registries);

        BotiInteriorMeshCache.applySnapshot(
                id,
                3,
                Map.of(pos, chestState),
                Map.of(pos, newerNbt)
        );
        BotiInteriorMeshCache.applySnapshot(
                id,
                2,
                Map.of(pos, DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()),
                Map.of()
        );

        assertEquals(chestState, BotiInteriorMeshCache.getVisibleBlocks(id).get(pos));
        assertEquals(1, BotiInteriorMeshCache.getBlockEntityNbtCount(id));
    }

    @Test
    void invalidate_ClearsBlockEntityNbt() {
        UUID id = UUID.randomUUID();
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        CompoundTag nbt = BotiInteriorSampler.captureSyncNbt(new ChestBlockEntity(pos, chestState), registries);
        BotiInteriorMeshCache.applySnapshot(id, 1, Map.of(pos, chestState), Map.of(pos, nbt));

        BotiInteriorMeshCache.invalidate(id);

        assertFalse(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(0, BotiInteriorMeshCache.getBlockEntityNbtCount(id));
    }

    @Test
    void applySnapshot_StoresEntitySamples() {
        UUID id = UUID.randomUUID();
        BlockPos wall = new BlockPos(0, 0, 0);
        CompoundTag entityNbt = new CompoundTag();
        entityNbt.putString("id", "minecraft:armor_stand");
        BotiEntitySample sample = new BotiEntitySample(4f, 1f, 5f, 45f, 0f, entityNbt);

        BotiInteriorMeshCache.applySnapshot(
                id,
                1,
                Map.of(wall, DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()),
                Map.of(),
                List.of(sample)
        );

        assertTrue(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(1, BotiInteriorMeshCache.getEntitySampleCount(id));
        // Without a client world, synthetic entities are not rebuilt yet.
        assertTrue(BotiInteriorMeshCache.getEntities(id).isEmpty());
    }

    @Test
    void applySnapshot_OlderRevisionDoesNotReplaceEntities() {
        UUID id = UUID.randomUUID();
        BlockPos wall = new BlockPos(0, 0, 0);
        CompoundTag newerNbt = new CompoundTag();
        newerNbt.putString("id", "minecraft:armor_stand");
        BotiEntitySample newer = new BotiEntitySample(1f, 1f, 1f, 0f, 0f, newerNbt);

        BotiInteriorMeshCache.applySnapshot(
                id,
                4,
                Map.of(wall, DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()),
                Map.of(),
                List.of(newer)
        );
        BotiInteriorMeshCache.applySnapshot(
                id,
                3,
                Map.of(wall, DWMBlocks.TEAL_BIG_ROUNDEL_A.defaultBlockState()),
                Map.of(),
                List.of()
        );

        assertEquals(1, BotiInteriorMeshCache.getEntitySampleCount(id));
        assertEquals(
                DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState(),
                BotiInteriorMeshCache.getVisibleBlocks(id).get(wall)
        );
    }

    @Test
    void invalidate_ClearsEntitySamples() {
        UUID id = UUID.randomUUID();
        CompoundTag entityNbt = new CompoundTag();
        entityNbt.putString("id", "minecraft:pig");
        BotiInteriorMeshCache.applySnapshot(
                id,
                1,
                Map.of(new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()),
                Map.of(),
                List.of(new BotiEntitySample(2f, 1f, 2f, 0f, 0f, entityNbt))
        );

        BotiInteriorMeshCache.invalidate(id);

        assertFalse(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(0, BotiInteriorMeshCache.getEntitySampleCount(id));
    }
}
