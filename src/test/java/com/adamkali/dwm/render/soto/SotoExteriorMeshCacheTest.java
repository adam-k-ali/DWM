package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SotoExteriorMeshCacheTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void tearDown() {
        SotoExteriorMeshCache.invalidateAll();
    }

    @Test
    void getVisibleBlocks_emptyWithoutSnapshot() {
        UUID id = UUID.randomUUID();
        assertTrue(SotoExteriorMeshCache.getVisibleBlocks(id).isEmpty());
        assertFalse(SotoExteriorMeshCache.hasSnapshot(id));
        assertNull(SotoExteriorMeshCache.getShellState(id));
    }

    @Test
    void applySnapshot_storesBlocksAndShellMetadata() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> blocks = Map.of(
                new BlockPos(1, 1, 1), Blocks.STONE.getDefaultState()
        );
        SotoExteriorMeshCache.applySnapshot(
                id,
                1,
                blocks,
                Map.of(),
                List.of(),
                TardisChameleonVariant.FIRST_DOCTOR_BOX,
                0.75f,
                true,
                4
        );

        assertTrue(SotoExteriorMeshCache.hasSnapshot(id));
        assertEquals(blocks, SotoExteriorMeshCache.getVisibleBlocks(id));
        SotoExteriorMeshCache.ShellState shell = SotoExteriorMeshCache.getShellState(id);
        assertNotNull(shell);
        assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, shell.variant());
        assertEquals(0.75f, shell.doorSwing(), 1e-4f);
        assertTrue(shell.isOpen());
        assertEquals(4, shell.exteriorRotation());
    }

    @Test
    void applySnapshot_ignoresOlderRevision() {
        UUID id = UUID.randomUUID();
        Map<BlockPos, BlockState> newer = Map.of(new BlockPos(2, 2, 2), Blocks.DIRT.getDefaultState());
        Map<BlockPos, BlockState> older = Map.of(new BlockPos(3, 3, 3), Blocks.SAND.getDefaultState());
        SotoExteriorMeshCache.applySnapshot(
                id, 5, newer, Map.of(), List.of(),
                TardisChameleonVariant.TT_CAPSULE, 1.0f, true, 0
        );
        SotoExteriorMeshCache.applySnapshot(
                id, 4, older, Map.of(), List.of(),
                TardisChameleonVariant.FIRST_DOCTOR_BOX, 0.0f, false, 8
        );

        assertEquals(newer, SotoExteriorMeshCache.getVisibleBlocks(id));
        assertEquals(TardisChameleonVariant.TT_CAPSULE, SotoExteriorMeshCache.getShellState(id).variant());
    }

    @Test
    void applySnapshot_allowsEmptyBlocksWithShell() {
        UUID id = UUID.randomUUID();
        SotoExteriorMeshCache.applySnapshot(
                id,
                1,
                Map.of(),
                Map.of(),
                List.of(),
                TardisChameleonVariant.SECOND_DOCTOR_BOX,
                0.5f,
                true,
                2
        );

        assertTrue(SotoExteriorMeshCache.hasSnapshot(id));
        assertTrue(SotoExteriorMeshCache.getVisibleBlocks(id).isEmpty());
        assertEquals(TardisChameleonVariant.SECOND_DOCTOR_BOX, SotoExteriorMeshCache.getShellState(id).variant());
    }

    @Test
    void applySnapshot_storesEntitySamples() {
        UUID id = UUID.randomUUID();
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", "minecraft:pig");
        BotiEntitySample sample = new BotiEntitySample(1.0f, 2.0f, 3.0f, 45.0f, 0.0f, nbt);

        SotoExteriorMeshCache.applySnapshot(
                id,
                1,
                Map.of(new BlockPos(0, 0, 0), Blocks.STONE.getDefaultState()),
                Map.of(),
                List.of(sample),
                TardisChameleonVariant.TT_CAPSULE,
                1.0f,
                true,
                0
        );

        assertEquals(1, SotoExteriorMeshCache.getEntitySampleCount(id));
    }
}
