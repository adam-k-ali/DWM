package com.adamkali.dwm.data.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Codecs that expose vanilla constants (e.g. {@code MapColor.TERRACOTTA_ORANGE}, {@code SoundType.METAL}) and enums
 * to JSON by their lower-case field name ({@code "terracotta_orange"}, {@code "metal"}).
 */
public final class NamedValues {
    private NamedValues() {
    }

    /** All {@code public static} fields of {@code owner} that hold a {@code type}, keyed by lower-case name. */
    public static <T> Map<String, T> constants(Class<?> owner, Class<T> type) {
        Map<String, T> values = new LinkedHashMap<>();
        for (Field field : owner.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                try {
                    values.put(field.getName().toLowerCase(Locale.ROOT), type.cast(field.get(null)));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot read " + owner.getName() + "." + field.getName(), e);
                }
            }
        }
        return Map.copyOf(values);
    }

    public static <T extends Enum<T>> Map<String, T> enumConstants(Class<T> type) {
        Map<String, T> values = new LinkedHashMap<>();
        for (T constant : type.getEnumConstants()) {
            values.put(constant.name().toLowerCase(Locale.ROOT), constant);
        }
        return Map.copyOf(values);
    }

    /** Codec over a name → value table; encoding finds the name by identity. */
    public static <T> Codec<T> codec(String what, Map<String, T> table) {
        return Codec.STRING.comapFlatMap(
                name -> {
                    T value = table.get(name);
                    return value != null
                            ? DataResult.success(value)
                            : DataResult.error(() -> "Unknown " + what + " '" + name + "'; expected one of " + table.keySet().stream().sorted().toList());
                },
                value -> table.entrySet().stream()
                        .filter(entry -> entry.getValue() == value)
                        .map(Map.Entry::getKey)
                        .sorted()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Unregistered " + what + " " + value))
        );
    }

    public static <T extends Enum<T>> Codec<T> enumCodec(String what, Class<T> type) {
        return codec(what, enumConstants(type));
    }
}
