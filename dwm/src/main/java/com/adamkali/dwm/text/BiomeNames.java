package com.adamkali.dwm.text;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Localizes biome registry ids for UI (e.g. {@code minecraft:plains} → {@code biome.minecraft.plains}).
 * When no translation exists, falls back to a title-cased path like {@link DimensionNames}.
 */
public final class BiomeNames {
    private BiomeNames() {
    }

    /**
     * Returns a localized display name for a biome id string.
     * Blank/null → empty; unparseable → literal id; otherwise translation with humanized-path fallback.
     */
    public static Component of(@Nullable String biomeId) {
        if (biomeId == null || biomeId.isBlank()) {
            return Component.empty();
        }
        Identifier id = Identifier.tryParse(biomeId);
        if (id == null) {
            return Component.literal(biomeId);
        }
        return Component.translatableWithFallback(id.toLanguageKey("biome"), DimensionNames.fallbackName(id));
    }
}
