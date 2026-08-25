package com.adamkali.dwm.tardis.worldgen;

import com.adamkali.dwm.DWMReference;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public final class DWMStructureProcessors {
    private DWMStructureProcessors() {
    }

    public static void initialize() {
        register("tardis_worldgen_marker", TardisWorldgenMarkerProcessor.MAP_CODEC);
    }

    private static void register(String path, MapCodec<? extends StructureProcessor> codec) {
        Registry.register(
                BuiltInRegistries.STRUCTURE_PROCESSOR,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path),
                codec
        );
    }
}
