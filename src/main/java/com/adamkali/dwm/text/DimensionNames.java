package com.adamkali.dwm.text;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Localizes dimension registry ids for UI (e.g. {@code dwm:gallifrey} → {@code dimension.dwm.gallifrey}).
 */
public final class DimensionNames {
    private DimensionNames() {
    }

    /**
     * Returns a localized display name for a dimension id string from network payloads.
     * Blank/null → empty; unparseable → literal id; otherwise translation with id fallback.
     */
    public static Component of(@Nullable String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return Component.empty();
        }
        Identifier id = Identifier.tryParse(dimensionId);
        if (id == null) {
            return Component.literal(dimensionId);
        }
        return of(id);
    }

    /**
     * Returns a localized display name for a dimension {@link Identifier}.
     */
    public static Component of(Identifier dimensionId) {
        return Component.translatableWithFallback(dimensionId.toLanguageKey("dimension"), dimensionId.toString());
    }
}
