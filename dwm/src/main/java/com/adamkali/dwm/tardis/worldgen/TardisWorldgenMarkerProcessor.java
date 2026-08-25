package com.adamkali.dwm.tardis.worldgen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

/**
 * Marks worldgen-placed TARDIS exteriors so first UUID assignment creates an unfinished Type 40.
 */
public final class TardisWorldgenMarkerProcessor implements StructureProcessor {
    public static final TardisWorldgenMarkerProcessor INSTANCE = new TardisWorldgenMarkerProcessor();
    public static final MapCodec<TardisWorldgenMarkerProcessor> MAP_CODEC =
            MapCodec.unit(() -> INSTANCE);

    private TardisWorldgenMarkerProcessor() {
    }

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos targetPosition,
            BlockPos referencePos,
            BlockPos templateRelativePos,
            StructureTemplate.StructureBlockInfo processedBlockInfo,
            StructurePlaceSettings settings
    ) {
        if (!processedBlockInfo.state().is(DWMBlocks.TARDIS_BLOCK)) {
            return processedBlockInfo;
        }
        CompoundTag nbt = processedBlockInfo.nbt() == null
                ? new CompoundTag()
                : processedBlockInfo.nbt().copy();
        nbt.putBoolean(TardisBlockEntity.WORLDGEN_FOUND_KEY, true);
        return new StructureTemplate.StructureBlockInfo(
                processedBlockInfo.pos(),
                processedBlockInfo.state(),
                nbt
        );
    }

    @Override
    public MapCodec<TardisWorldgenMarkerProcessor> codec() {
        return MAP_CODEC;
    }
}
