package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-built exterior SOTO snapshot.
 * formatVersion 4 = signed relative block positions + radiusChunks + BE NBT + entities + shell.
 */
public record SotoExteriorSnapshot(
        int formatVersion,
        UUID tardisId,
        int revision,
        int radiusChunks,
        Map<BlockPos, BlockState> blocks,
        Map<BlockPos, NbtCompound> blockEntities,
        List<BotiEntitySample> entities,
        TardisChameleonVariant variant,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation
) {
    public static final int FORMAT_VERSION_VIEW_DISTANCE = 4;

    public SotoExteriorSnapshot {
        radiusChunks = SotoExteriorSampler.clampRadiusChunks(radiusChunks);
        blocks = Map.copyOf(blocks);
        blockEntities = copyNbtMap(blockEntities);
        entities = copyEntities(entities);
        if (variant == null) {
            variant = TardisChameleonVariant.TT_CAPSULE;
        }
    }

    public static SotoExteriorSnapshot of(
            UUID tardisId,
            int revision,
            int radiusChunks,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities,
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation
    ) {
        return new SotoExteriorSnapshot(
                FORMAT_VERSION_VIEW_DISTANCE,
                tardisId,
                revision,
                radiusChunks,
                blocks,
                blockEntities,
                entities,
                variant,
                doorSwing,
                isOpen,
                exteriorRotation
        );
    }

    public Map<BlockPos, BlockState> blocksView() {
        return Collections.unmodifiableMap(blocks);
    }

    public Map<BlockPos, NbtCompound> blockEntitiesView() {
        return Collections.unmodifiableMap(blockEntities);
    }

    public List<BotiEntitySample> entitiesView() {
        return Collections.unmodifiableList(entities);
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
