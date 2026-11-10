package com.adamkali.dwm.render.soto.ghost;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Pure helper: which neighbouring chunk columns need a re-bake after a chunk changed.
 *
 * <p>Ambient occlusion and light sampling reach about one block across a chunk border, so a
 * neighbour's mesh can only be affected by a change in the one-block-wide strip (or corner)
 * of the changed chunk that touches it. Interior edits affect the chunk alone.
 */
final class GhostNeighborDirty {
    private static final int CHUNK_SIZE = 16;
    private static final int LAST = CHUNK_SIZE - 1;

    private GhostNeighborDirty() {
    }

    /**
     * @param changedRelPositions footprint-relative positions (the chunk block map's key space) whose
     *                            state changed, appeared or disappeared
     * @param originX             footprint origin X; world X = origin + rel (chunks are world-aligned,
     *                            the footprint origin need not be)
     * @param originZ             footprint origin Z
     * @return packed {@link ChunkPos} keys of neighbours (never the chunk itself) that border a change
     */
    static Set<Long> neighborsToRebake(
            Collection<BlockPos> changedRelPositions, int originX, int originZ, int chunkX, int chunkZ
    ) {
        Set<Long> result = new LinkedHashSet<>();
        if (changedRelPositions == null) {
            return result;
        }
        for (BlockPos pos : changedRelPositions) {
            if (pos == null) {
                continue;
            }
            int dx = edgeDirection(Math.floorMod(originX + pos.getX(), CHUNK_SIZE));
            int dz = edgeDirection(Math.floorMod(originZ + pos.getZ(), CHUNK_SIZE));
            if (dx != 0) {
                result.add(ChunkPos.pack(chunkX + dx, chunkZ));
            }
            if (dz != 0) {
                result.add(ChunkPos.pack(chunkX, chunkZ + dz));
            }
            if (dx != 0 && dz != 0) {
                result.add(ChunkPos.pack(chunkX + dx, chunkZ + dz));
            }
        }
        return result;
    }

    /** All eight surrounding chunks (used when change locality is unknown, e.g. light data). */
    static Set<Long> allNeighbors(int chunkX, int chunkZ) {
        Set<Long> result = new LinkedHashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    result.add(ChunkPos.pack(chunkX + dx, chunkZ + dz));
                }
            }
        }
        return result;
    }

    private static int edgeDirection(int local) {
        if (local == 0) {
            return -1;
        }
        if (local == LAST) {
            return 1;
        }
        return 0;
    }
}
