package com.adamkali.sightline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScenarioCoordinates {
    private static final Pattern RELATIVE = Pattern.compile("^~([+-]?\\d+)?$");
    private static final Pattern ABSOLUTE = Pattern.compile("^-?\\d+$");

    private ScenarioCoordinates() {
    }

    public record Component(boolean relative, int value) {
        public int resolve(int origin) {
            return relative ? origin + value : value;
        }

        public String authored() {
            if (!relative) {
                return Integer.toString(value);
            }
            return value == 0 ? "~" : "~" + value;
        }
    }

    public static Component parse(Object value, String field) {
        if (value == null) {
            throw new ScenarioException(field + " must be a relative (~, ~1) or absolute integer; quote \"~\" in YAML");
        }
        if (value instanceof Number number) {
            return absoluteNumber(number, field);
        }
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(field + " must be a relative (~, ~1) or absolute integer");
        }
        String trimmed = string.trim();
        Matcher relative = RELATIVE.matcher(trimmed);
        if (relative.matches()) {
            String offset = relative.group(1);
            return new Component(true, offset == null ? 0 : Integer.parseInt(offset));
        }
        if (ABSOLUTE.matcher(trimmed).matches()) {
            return new Component(false, Integer.parseInt(trimmed));
        }
        throw new ScenarioException(field + " must be a relative (~, ~1) or absolute integer");
    }

    private static Component absoluteNumber(Number number, String field) {
        double value = number.doubleValue();
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ScenarioException(field + " must be a relative (~, ~1) or absolute integer");
        }
        return new Component(false, number.intValue());
    }
}
