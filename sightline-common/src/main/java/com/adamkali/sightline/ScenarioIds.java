package com.adamkali.sightline;

import net.minecraft.resources.Identifier;

public final class ScenarioIds {
    private ScenarioIds() {
    }

    public static String normalize(Object value, String field) {
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(field + " requires a non-empty namespaced id");
        }
        String trimmed = string.trim();
        try {
            Identifier parsed = trimmed.contains(":")
                    ? Identifier.parse(trimmed)
                    : Identifier.withDefaultNamespace(trimmed);
            return parsed.toString();
        } catch (RuntimeException exception) {
            throw new ScenarioException(field + " '" + trimmed + "' is not a valid id");
        }
    }
}
