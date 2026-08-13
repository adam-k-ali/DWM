package com.adamkali.dwm.world.village;

import com.adamkali.dwm.DWMReference;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public final class GallifreyVillageProcessor implements StructureProcessor {
    public static final GallifreyVillageProcessor INSTANCE = new GallifreyVillageProcessor();
    public static final MapCodec<GallifreyVillageProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private GallifreyVillageProcessor() {
    }

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_village");
        if (BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(id)) {
            return;
        }
        Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, CODEC);
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo processBlock(
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
