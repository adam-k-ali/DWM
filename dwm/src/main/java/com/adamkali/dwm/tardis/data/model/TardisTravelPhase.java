package com.adamkali.dwm.tardis.data.model;

import org.jetbrains.annotations.Nullable;

/**
 * Exterior travel phases for dematerialisation / materialisation.
 */
public enum TardisTravelPhase {
    IDLE,
    DEMATERIALISING,
    IN_FLIGHT,
    MATERIALISING;

    public static TardisTravelPhase fromString(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return IDLE;
        }
        try {
            return TardisTravelPhase.valueOf(value);
        } catch (IllegalArgumentException e) {
            return IDLE;
        }
    }

    public boolean isTraveling() {
        return this != IDLE;
    }

    /** True when a second lever pull may request materialisation. */
    public boolean awaitsMaterialise() {
        return this == IN_FLIGHT;
    }
}
