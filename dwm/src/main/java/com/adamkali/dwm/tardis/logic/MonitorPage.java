package com.adamkali.dwm.tardis.logic;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Pages shown on a TARDIS monitor. Declaration order is the cycle order.
 */
public enum MonitorPage {
    LOCATION,
    DESTINATION,
    SYSTEMS,
    ATMOSPHERE;

    public MonitorPage next() {
        MonitorPage[] pages = values();
        return pages[(ordinal() + 1) % pages.length];
    }

    public MonitorPage previous() {
        MonitorPage[] pages = values();
        return pages[(ordinal() + pages.length - 1) % pages.length];
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String titleKey() {
        return "dwm.monitor.page." + serializedName();
    }

    public static MonitorPage fromSerializedName(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return LOCATION;
        }
        for (MonitorPage page : values()) {
            if (page.serializedName().equals(value)) {
                return page;
            }
        }
        return LOCATION;
    }
}
