package com.adamkali.dwm.entity;

/**
 * Physical Dalek casing: one mesh per chassis. Each {@link DalekVariant} is painted for one chassis;
 * the chassis decides which {@link DalekFamily} it belongs to and where the gunstick muzzle sits
 * (blocks, relative to the entity origin).
 */
public enum DalekChassis {
    CLASSIC_1963(DalekFamily.CLASSIC, 16.5F / 16.0F, 12.0F / 16.0F, -2.0F / 16.0F),
    TIME_WAR(DalekFamily.CLASSIC, 16.5F / 16.0F, 13.0F / 16.0F, -2.0F / 16.0F),
    PARADIGM(DalekFamily.PARADIGM, 23.0F / 16.0F, 15.0F / 16.0F, -3.5F / 16.0F),
    SPECIAL_WEAPONS(DalekFamily.SPECIAL_WEAPONS, 20.5F / 16.0F, 18.0F / 16.0F, 0.0F);

    private final DalekFamily family;
    private final float gunHeight;
    private final float gunForward;
    private final float gunSide;

    DalekChassis(DalekFamily family, float gunHeight, float gunForward, float gunSide) {
        this.family = family;
        this.gunHeight = gunHeight;
        this.gunForward = gunForward;
        this.gunSide = gunSide;
    }

    public DalekFamily family() {
        return family;
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
}
