package com.adamkali.dwm.tardis.boti;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-built BOTI placement snapshot. formatVersion 2 includes block states plus
 * chunk-sync block-entity NBT for client BER rendering.
 */
public record BotiInteriorSnapshot(
        int formatVersion,
        UUID tardisId,
        int revision,
        Map<BlockPos, BlockState> blocks,
        Map<BlockPos, NbtCompound> blockEntities
) {
    /** @deprecated Prefer {@link #FORMAT_VERSION_BLOCKS_AND_BES}; kept for test comparisons. */
    @Deprecated
    public static final int FORMAT_VERSION_BLOCKS = 1;
    public static final int FORMAT_VERSION_BLOCKS_AND_BES = 2;

    public BotiInteriorSnapshot {
        blocks = Map.copyOf(blocks);
        blockEntities = copyNbtMap(blockEntities);
    }

    public static BotiInteriorSnapshot of(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities
    ) {
        return new BotiInteriorSnapshot(FORMAT_VERSION_BLOCKS_AND_BES, tardisId, revision, blocks, blockEntities);
    }

    public static BotiInteriorSnapshot of(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        return of(tardisId, revision, blocks, Map.of());
    }

    public Map<BlockPos, BlockState> blocksView() {
        return Collections.unmodifiableMap(blocks);
    }

    public Map<BlockPos, NbtCompound> blockEntitiesView() {
        return Collections.unmodifiableMap(blockEntities);
    }

    private static Map<BlockPos, NbtCompound> copyNbtMap(Map<BlockPos, NbtCompound> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<BlockPos, NbtCompound> copy = new HashMap<>(source.size());
        for (Map.Entry<BlockPos, NbtCompound> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return Map.copyOf(copy);
    }
}
