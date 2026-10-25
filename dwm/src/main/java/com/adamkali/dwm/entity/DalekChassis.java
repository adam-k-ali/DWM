package com.adamkali.dwm.entity;

/**
 * Physical Dalek casing. Each entity type has one chassis; it decides which {@link DalekVariant}
 * skins fit its mesh and where the gunstick muzzle sits (blocks, relative to the entity origin).
 */
public enum DalekChassis {
    CLASSIC(18.0F / 16.0F, 10.0F / 16.0F, -2.5F / 16.0F),
    PARADIGM(23.0F / 16.0F, 15.0F / 16.0F, -3.5F / 16.0F);

    private final float gunHeight;
    private final float gunForward;
    private final float gunSide;

    DalekChassis(float gunHeight, float gunForward, float gunSide) {
        this.gunHeight = gunHeight;
        this.gunForward = gunForward;
        this.gunSide = gunSide;
    }

    public float gunHeight() {
        return gunHeight;
    }

    public float gunForward() {
        return gunForward;
    }

    public float gunSide() {
        return gunSide;
    }

    /** First declared variant of this chassis; used when no variant was picked or the saved one does not fit. */
    public DalekVariant defaultVariant() {
        for (DalekVariant variant : DalekVariant.values()) {
            if (variant.chassis() == this) {
                return variant;
            }
        }
        throw new IllegalStateException("No Dalek variant for chassis " + this);
    }
}
