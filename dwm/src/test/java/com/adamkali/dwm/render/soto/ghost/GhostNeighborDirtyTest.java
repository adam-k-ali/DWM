package com.adamkali.dwm.render.soto.ghost;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostNeighborDirtyTest {

    private static long key(int x, int z) {
        return ChunkPos.pack(x, z);
    }

    @Test
    void interiorChange_affectsNoNeighbour() {
        Set<Long> result = GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(8, 5, 8)), 0, 0, 3, 4);
        assertTrue(result.isEmpty());
    }

    @Test
    void edgeChange_affectsOneNeighbour() {
        assertEquals(Set.of(key(2, 4)), GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(0, 5, 8)), 0, 0, 3, 4));
        assertEquals(Set.of(key(4, 4)), GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(15, 5, 8)), 0, 0, 3, 4));
        assertEquals(Set.of(key(3, 3)), GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(8, 5, 0)), 0, 0, 3, 4));
        assertEquals(Set.of(key(3, 5)), GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(8, 5, 15)), 0, 0, 3, 4));
    }

    @Test
    void cornerChange_affectsThreeNeighbours() {
        Set<Long> result = GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(0, 5, 15)), 0, 0, 3, 4);
        assertEquals(Set.of(key(2, 4), key(3, 5), key(2, 5)), result);
    }

    @Test
    void nonChunkAlignedOrigin_usesWorldCoordinates() {
        // Origin x = 100: rel 12 -> world 112 -> local 0 (west edge); rel 2 -> world 102 -> interior.
        assertEquals(
                Set.of(key(2, 4)),
                GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(12, 1, 3)), 100, 200, 3, 4)
        );
        assertTrue(GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(2, 1, 3)), 100, 200, 3, 4).isEmpty());
    }

    @Test
    void negativeWorldCoordinates_useFloorMod() {
        // origin -16 + rel 0 = world -16 -> local 0.
        assertEquals(
                Set.of(key(2, 4)),
                GhostNeighborDirty.neighborsToRebake(List.of(new BlockPos(0, 0, 8)), -16, 0, 3, 4)
        );
    }

    @Test
    void multipleChanges_areUnioned() {
        Set<Long> result = GhostNeighborDirty.neighborsToRebake(
                List.of(new BlockPos(0, 1, 8), new BlockPos(15, 1, 8), new BlockPos(8, 1, 8)), 0, 0, 0, 0
        );
        assertEquals(Set.of(key(-1, 0), key(1, 0)), result);
    }

    @Test
    void nullOrEmpty_isEmpty() {
        assertTrue(GhostNeighborDirty.neighborsToRebake(null, 0, 0, 0, 0).isEmpty());
        assertTrue(GhostNeighborDirty.neighborsToRebake(List.of(), 0, 0, 0, 0).isEmpty());
    }

    @Test
    void allNeighbors_isEightAndExcludesSelf() {
        Set<Long> result = GhostNeighborDirty.allNeighbors(5, 6);
        assertEquals(8, result.size());
        assertTrue(!result.contains(key(5, 6)));
    }
}
