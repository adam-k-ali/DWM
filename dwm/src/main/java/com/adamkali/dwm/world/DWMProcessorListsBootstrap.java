package com.adamkali.dwm.world;

import com.adamkali.dwm.world.village.GallifreyVillageProcessor;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.List;

public final class DWMProcessorListsBootstrap {
    private DWMProcessorListsBootstrap() {
    }

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        context.register(
                DWMProcessorLists.GALLIFREY_VILLAGE,
                new StructureProcessorList(List.of(GallifreyVillageProcessor.INSTANCE))
        );
    }
}
