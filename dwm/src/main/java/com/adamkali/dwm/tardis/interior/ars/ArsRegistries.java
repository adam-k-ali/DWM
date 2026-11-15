package com.adamkali.dwm.tardis.interior.ars;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Synced datapack registry for Architectural Reconfiguration System piece definitions.
 *
 * <p>Keys use the vanilla {@code minecraft} namespace so Fabric does not prepend a registry
 * namespace folder. Entries therefore load from {@code data/<entry ns>/tardis/ars_piece/...} — for
 * example {@code data/dwm/tardis/ars_piece/library.json}. A {@code dwm:} registry id would load from
 * {@code data/<entry ns>/dwm/tardis/ars_piece/...} instead.
 *
 * <p>The registry may be empty: Fabric registers dynamic registries with no non-empty validator.
 */
public final class ArsRegistries {
    public static final ResourceKey<Registry<ArsPieceDefinition>> PIECE = ResourceKey.createRegistryKey(
            Identifier.withDefaultNamespace("tardis/ars_piece")
    );

    private ArsRegistries() {
    }

    public static void initialize() {
        DynamicRegistries.registerSynced(PIECE, ArsPieceDefinition.CODEC);
    }
}
