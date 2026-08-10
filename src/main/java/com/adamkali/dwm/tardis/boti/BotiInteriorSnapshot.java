package com.adamkali.dwm.tardis.boti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server-built BOTI placement snapshot. formatVersion 3 includes block states,
 * chunk-sync block-entity NBT, and live entity samples for client rendering.
 */
public record BotiInteriorSnapshot(
        int formatVersion,
        UUID tardisId,
        int revision,
        Map<BlockPos, BlockState> blocks,
        Map<BlockPos, CompoundTag> blockEntities,
        List<BotiEntitySample> entities
) {
    /** @deprecated Prefer {@link #FORMAT_VERSION_BLOCKS_BES_AND_ENTITIES}; kept for test comparisons. */
    @Deprecated
    public static final int FORMAT_VERSION_BLOCKS = 1;
    /** @deprecated Prefer {@link #FORMAT_VERSION_BLOCKS_BES_AND_ENTITIES}; kept for test comparisons. */
    @Deprecated
    public static final int FORMAT_VERSION_BLOCKS_AND_BES = 2;
    public static final int FORMAT_VERSION_BLOCKS_BES_AND_ENTITIES = 3;

    public BotiInteriorSnapshot {
        blocks = Map.copyOf(blocks);
        blockEntities = copyNbtMap(blockEntities);
        entities = copyEntities(entities);
    }

    public static BotiInteriorSnapshot of(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntities,
            List<BotiEntitySample> entities
    ) {
        return new BotiInteriorSnapshot(
                FORMAT_VERSION_BLOCKS_BES_AND_ENTITIES,
                tardisId,
                revision,
                blocks,
                blockEntities,
                entities
        );
    }

    public static BotiInteriorSnapshot of(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntities
    ) {
        return of(tardisId, revision, blocks, blockEntities, List.of());
    }

    public static BotiInteriorSnapshot of(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        return of(tardisId, revision, blocks, Map.of(), List.of());
    }

    public Map<BlockPos, BlockState> blocksView() {
        return Collections.unmodifiableMap(blocks);
    }

    public Map<BlockPos, CompoundTag> blockEntitiesView() {
        return Collections.unmodifiableMap(blockEntities);
    }

    public List<BotiEntitySample> entitiesView() {
        return Collections.unmodifiableList(entities);
    }

    private static Map<BlockPos, CompoundTag> copyNbtMap(Map<BlockPos, CompoundTag> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<BlockPos, CompoundTag> copy = new HashMap<>(source.size());
        for (Map.Entry<BlockPos, CompoundTag> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return Map.copyOf(copy);
    }

    private static List<BotiEntitySample> copyEntities(List<BotiEntitySample> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<BotiEntitySample> copy = new ArrayList<>(source.size());
        for (BotiEntitySample sample : source) {
            if (sample != null) {
                copy.add(sample);
            }
        }
        return List.copyOf(copy);
    }
}
