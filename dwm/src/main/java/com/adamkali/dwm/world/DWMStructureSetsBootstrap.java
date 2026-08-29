package com.adamkali.dwm.world;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public final class DWMStructureSetsBootstrap {
    private DWMStructureSetsBootstrap() {
    }

    public static void bootstrap(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(
                DWMStructureSets.GALLIFREY_VILLAGE,
                new StructureSet(
                        structures.getOrThrow(DWMStructures.GALLIFREY_VILLAGE),
                        new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, DWMStructureSets.PLACEMENT_SALT)
                )
        );
    }
}
