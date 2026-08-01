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
 * formatVersion 4 = blocks + BE NBT + entities + shell + atmosphere.
 */
public record SotoExteriorSnapshot(
        int formatVersion,
        UUID tardisId,
        int revision,
        Map<BlockPos, BlockState> blocks,
        Map<BlockPos, NbtCompound> blockEntities,
        List<BotiEntitySample> entities,
        TardisChameleonVariant variant,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation,
        SotoAtmosphere atmosphere
) {
    /** @deprecated Prefer {@link #FORMAT_VERSION_ATMOSPHERE}. */
    @Deprecated
    public static final int FORMAT_VERSION_BLOCKS_BES_ENTITIES_SHELL = 3;
    public static final int FORMAT_VERSION_ATMOSPHERE = 4;

    public SotoExteriorSnapshot {
        blocks = Map.copyOf(blocks);
        blockEntities = copyNbtMap(blockEntities);
        entities = copyEntities(entities);
        if (variant == null) {
            variant = TardisChameleonVariant.TT_CAPSULE;
        }
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
    }

    public static SotoExteriorSnapshot of(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities,
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation,
            SotoAtmosphere atmosphere
    ) {
        return new SotoExteriorSnapshot(
                FORMAT_VERSION_ATMOSPHERE,
                tardisId,
                revision,
                blocks,
                blockEntities,
                entities,
                variant,
                doorSwing,
                isOpen,
                exteriorRotation,
                atmosphere
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
