package com.adamkali.dwm.text;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Localizes dimension registry ids for UI (e.g. {@code dwm:gallifrey} → {@code dimension.dwm.gallifrey}).
 * When no translation exists, falls back to a title-cased path (e.g. {@code the_nether} → {@code The Nether}).
 */
public final class DimensionNames {
    private DimensionNames() {
    }

    /**
     * Returns a localized display name for a dimension id string from network payloads.
     * Blank/null → empty; unparseable → literal id; otherwise translation with humanized-path fallback.
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
        return Component.translatableWithFallback(
                dimensionId.toLanguageKey("dimension"),
                fallbackName(dimensionId)
        );
    }

    /**
     * Title-cases the identifier path for display when no translation exists.
     * Splits on {@code _} and {@code /}; e.g. {@code the_nether} → {@code The Nether}.
     */
    static String fallbackName(Identifier dimensionId) {
        String path = dimensionId.getPath();
        if (path == null || path.isBlank()) {
            return dimensionId.toString();
        }
        StringBuilder result = new StringBuilder(path.length());
        for (String segment : path.split("[_/]")) {
            if (segment.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(segment.charAt(0)));
            if (segment.length() > 1) {
                result.append(segment, 1, segment.length());
            }
        }
        if (result.isEmpty()) {
            return dimensionId.toString();
        }
        return result.toString();
    }
}
