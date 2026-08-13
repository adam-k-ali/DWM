package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class DWMProcessorLists {
    public static final ResourceKey<StructureProcessorList> GALLIFREY_VILLAGE = ResourceKey.create(
            Registries.PROCESSOR_LIST,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_village")
    );

    private DWMProcessorLists() {
    }
}
