package com.adamkali.dwm.world.village;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public final class GallifreyVillageProcessor implements StructureProcessor {
    public static final GallifreyVillageProcessor INSTANCE = new GallifreyVillageProcessor();
    public static final MapCodec<GallifreyVillageProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private GallifreyVillageProcessor() {
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
        BlockState original = processedBlockInfo.state();
        BlockState replaced = GallifreyVillagePalette.replace(original, settings.getRandom(processedBlockInfo.pos()));
        if (replaced == original) {
            return processedBlockInfo;
        }
        return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), replaced, processedBlockInfo.nbt());
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }
}
