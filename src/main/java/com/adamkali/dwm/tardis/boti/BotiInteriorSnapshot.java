package com.adamkali.dwm.tardis.boti;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Server-built BOTI placement snapshot. formatVersion 1 is blocks only;
 * later versions may attach block-entity visuals.
 */
public record BotiInteriorSnapshot(
        int formatVersion,
        UUID tardisId,
        int revision,
        Map<BlockPos, BlockState> blocks
) {
    public static final int FORMAT_VERSION_BLOCKS = 1;

    public BotiInteriorSnapshot {
        blocks = Map.copyOf(blocks);
    }

    public static BotiInteriorSnapshot of(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        return new BotiInteriorSnapshot(FORMAT_VERSION_BLOCKS, tardisId, revision, blocks);
    }

    public Map<BlockPos, BlockState> blocksView() {
        return Collections.unmodifiableMap(blocks);
    }
}
