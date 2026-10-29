package com.adamkali.dwm.entity;

import net.minecraft.util.StringRepresentable;

/**
 * Role a garrison Dalek performs at and around its post. Wild Daleks are {@link #NONE} and keep
 * the default wandering behaviour.
 */
public enum DalekJob implements StringRepresentable {
    NONE("none"),
    SENTRY("sentry"),
    PATROL("patrol"),
    LOOKOUT("lookout"),
    TECHNICIAN("technician");

    private final String id;

    DalekJob(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public static DalekJob byId(String id) {
        for (DalekJob job : values()) {
            if (job.id.equals(id)) {
                return job;
            }
        }
        return NONE;
    }

    public static DalekJob byOrdinal(int ordinal) {
        DalekJob[] jobs = values();
        return ordinal >= 0 && ordinal < jobs.length ? jobs[ordinal] : NONE;
    }

    /** Sentries and lookouts stand at their post and scan. */
    public boolean holdsPost() {
        return this == SENTRY || this == LOOKOUT;
    }
}
