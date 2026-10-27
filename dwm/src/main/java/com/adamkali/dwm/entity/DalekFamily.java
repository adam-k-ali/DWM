package com.adamkali.dwm.entity;

/**
 * Dalek line an entity type spawns from. Each entity type has one family; it decides which
 * {@link DalekVariant}s the entity may wear, across one or more {@link DalekChassis} meshes.
 */
public enum DalekFamily {
    CLASSIC,
    PARADIGM,
    SPECIAL_WEAPONS;

    /** First declared variant of this family; used when no variant was picked or the saved one does not fit. */
    public DalekVariant defaultVariant() {
        for (DalekVariant variant : DalekVariant.values()) {
            if (variant.family() == this) {
                return variant;
            }
        }
        throw new IllegalStateException("No Dalek variant for family " + this);
    }
}
