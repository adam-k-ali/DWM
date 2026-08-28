package com.adamkali.dwm.tardis.data.model;

import java.util.Locale;

/**
 * Console / remote circuits that can be fitted (working) or broken on a found Type 40.
 */
public enum TardisCircuit {
    PLANET_LOCATOR,
    WAYPOINTS,
    PLAYER_LOCATOR,
    TELEPATHIC,
    FAST_RETURN,
    CLOAK,
    CHAMELEON,
    COORDINATE_LOCKS,
    STABILISERS,
    REMOTE_SUMMON;

    public String translationKey() {
        return "dwm.circuit." + name().toLowerCase(Locale.ROOT);
    }
}
