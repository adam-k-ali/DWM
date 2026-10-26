package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Registered {@link StructureType}s of DWM. The structures themselves are data-driven
 * ({@code data/dwm/worldgen/structure/*.json}); only their types live in code. Loading this class registers them.
 */
public final class DWMStructureTypes {
    public static final StructureType<ThalSettlementStructure> THAL_SETTLEMENT =
            register("thal_settlement", () -> ThalSettlementStructure.CODEC);

    private DWMStructureTypes() {
    }

    /** Forces class loading (and therefore registration) during mod initialisation. */
    public static void initialize() {
    }

    private static <S extends Structure> StructureType<S> register(String path, StructureType<S> type) {
        return Registry.register(
                BuiltInRegistries.STRUCTURE_TYPE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path),
                type
        );
    }
}
