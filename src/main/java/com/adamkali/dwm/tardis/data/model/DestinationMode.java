package com.adamkali.dwm.tardis.data.model;

import org.jetbrains.annotations.Nullable;

/**
 * How the TARDIS resolves its next landing destination.
 */
public enum DestinationMode {
    /** Biome (and optional dimension) selection via console dials. */
    BIOME,
    /** Exact saved exterior waypoint. */
    WAYPOINT,
    /** Live position of a selected online player at materialise. */
    PLAYER,
    /** Exact historically visited exterior from fast-return history. */
    FAST_RETURN;

    public static DestinationMode fromString(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return BIOME;
        }
        try {
            return DestinationMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return BIOME;
        }
    }
}
