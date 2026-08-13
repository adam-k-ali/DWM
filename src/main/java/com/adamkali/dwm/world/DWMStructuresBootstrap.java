package com.adamkali.dwm.world;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Optional;

public final class DWMStructuresBootstrap {
    private DWMStructuresBootstrap() {
    }

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(
                DWMStructures.GALLIFREY_VILLAGE,
                new JigsawStructure(
                        new Structure.StructureSettings.Builder(biomes.getOrThrow(DWMBiomeTags.HAS_GALLIFREY_VILLAGE))
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .build(),
                        pools.getOrThrow(DWMVillagePools.TOWN_CENTERS),
                        Optional.empty(),
                        6,
                        ConstantHeight.of(VerticalAnchor.absolute(0)),
                        true,
                        Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                        new JigsawStructure.MaxDistance(80),
                        DWMVillagePools.poolAliases(),
                        JigsawStructure.DEFAULT_DIMENSION_PADDING,
                        JigsawStructure.DEFAULT_LIQUID_SETTINGS
                )
        );
    }
}
