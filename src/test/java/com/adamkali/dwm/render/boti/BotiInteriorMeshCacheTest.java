package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotiInteriorMeshCacheTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
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
}
