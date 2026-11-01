package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import com.mojang.serialization.MapCodec;

/** Registered {@link StructurePoolElementType}s of DWM. Loading this class registers them. */
public final class DWMStructurePoolElementTypes {
    public static final StructurePoolElementType<GarrisonedPoolElement> GARRISONED_SINGLE =
            register("garrisoned_single_pool_element", GarrisonedPoolElement.CODEC);

    private DWMStructurePoolElementTypes() {
    }

    /** Forces class loading (and therefore registration) during mod initialisation. */
    public static void initialize() {
    }

    private static <P extends StructurePoolElement> StructurePoolElementType<P> register(String path, MapCodec<P> codec) {
        return Registry.register(
                BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path),
                () -> codec
        );
    }
}
