package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/** Registered {@link StructurePieceType}s of DWM structures. Loading this class registers them. */
public final class DWMStructurePieceTypes {
    public static final StructurePieceType THAL_SHELTER = register("thal_shelter", (context, tag) -> new ThalShelterPiece(tag));
    public static final StructurePieceType THAL_HUT = register("thal_hut", (context, tag) -> new ThalHutPiece(tag));
    public static final StructurePieceType THAL_FIELD = register("thal_field", (context, tag) -> new ThalFieldPiece(tag));
    public static final StructurePieceType THAL_WATCH_POST = register("thal_watch_post", (context, tag) -> new ThalWatchPostPiece(tag));
    public static final StructurePieceType THAL_PATH = register("thal_path", (context, tag) -> new ThalPathPiece(tag));

    private DWMStructurePieceTypes() {
    }

    /** Forces class loading (and therefore registration) during mod initialisation. */
    public static void initialize() {
    }

    private static StructurePieceType register(String path, StructurePieceType type) {
        return Registry.register(
                BuiltInRegistries.STRUCTURE_PIECE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path),
                type
        );
    }
}
