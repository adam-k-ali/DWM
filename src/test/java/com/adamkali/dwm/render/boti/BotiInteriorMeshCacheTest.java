package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotiInteriorMeshCacheTest {

    private static RegistryWrapper.WrapperLookup registries;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        registries = BuiltinRegistries.createWrapperLookup();
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
    }

    @Test
    void applySnapshot_WinsOverBlueprint() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> live = Map.of(
                new BlockPos(1, 1, 1), DWMBlocks.WHITE_ROUNDEL_A.getDefaultState()
        );
        BotiInteriorMeshCache.applySnapshot(id, 1, live);

        assertTrue(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(live, BotiInteriorMeshCache.getVisibleBlocks(id));
    }

    @Test
    void applySnapshot_IgnoresOlderRevision() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> newer = Map.of(
                new BlockPos(2, 2, 2), DWMBlocks.TEAL_BIG_ROUNDEL_A.getDefaultState()
        );
        Map<BlockPos, BlockState> older = Map.of(
                new BlockPos(3, 3, 3), DWMBlocks.WHITE_TARDIS_WALL.getDefaultState()
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
        BlockState chestState = Blocks.CHEST.getDefaultState();
        ChestBlockEntity chest = new ChestBlockEntity(chestPos, chestState);
        NbtCompound nbt = BotiInteriorSampler.captureSyncNbt(chest, registries);

        BotiInteriorMeshCache.applySnapshot(
                id,
                1,
                Map.of(chestPos, chestState, new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.getDefaultState()),
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
        BlockState chestState = Blocks.CHEST.getDefaultState();
        NbtCompound newerNbt = BotiInteriorSampler.captureSyncNbt(new ChestBlockEntity(pos, chestState), registries);

        BotiInteriorMeshCache.applySnapshot(
                id,
                3,
                Map.of(pos, chestState),
                Map.of(pos, newerNbt)
        );
        BotiInteriorMeshCache.applySnapshot(
                id,
                2,
                Map.of(pos, DWMBlocks.WHITE_TARDIS_WALL.getDefaultState()),
                Map.of()
        );

        assertEquals(chestState, BotiInteriorMeshCache.getVisibleBlocks(id).get(pos));
        assertEquals(1, BotiInteriorMeshCache.getBlockEntityNbtCount(id));
    }

    @Test
    void invalidate_ClearsBlockEntityNbt() {
        UUID id = UUID.randomUUID();
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState chestState = Blocks.CHEST.getDefaultState();
        NbtCompound nbt = BotiInteriorSampler.captureSyncNbt(new ChestBlockEntity(pos, chestState), registries);
        BotiInteriorMeshCache.applySnapshot(id, 1, Map.of(pos, chestState), Map.of(pos, nbt));

        BotiInteriorMeshCache.invalidate(id);

        assertFalse(BotiInteriorMeshCache.hasSnapshot(id));
        assertEquals(0, BotiInteriorMeshCache.getBlockEntityNbtCount(id));
    }
}
