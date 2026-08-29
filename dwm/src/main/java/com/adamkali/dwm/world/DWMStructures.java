package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class DWMStructures {
    public static final ResourceKey<Structure> GALLIFREY_VILLAGE = key("gallifrey_village");

    private static ResourceKey<Structure> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMStructures() {
    }
}
