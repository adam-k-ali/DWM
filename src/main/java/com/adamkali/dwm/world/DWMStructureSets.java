package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public final class DWMStructureSets {
    public static final ResourceKey<StructureSet> GALLIFREY_VILLAGE = key("gallifrey_village");

    /** Unique salt; vanilla Overworld villages use 10387312. */
    public static final int PLACEMENT_SALT = 29473921;

    private static ResourceKey<StructureSet> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMStructureSets() {
    }
}
