package com.adamkali.dwm.entity;

/**
 * Tunable shape of a Dalek laser bolt: how fast it flies, how large it is drawn and how big a blast
 * it detonates on impact ({@code blastRadius} of 0 means a plain bolt with no explosion).
 */
public record DalekLaserProfile(float speed, float blastRadius, float visualScale) {
    /** The classic Dalek bolt. */
    public static final DalekLaserProfile STANDARD = new DalekLaserProfile(1.6F, 0.0F, 1.0F);
    /** The Special Weapons Dalek's explosive cannon round. */
    public static final DalekLaserProfile HEAVY = new DalekLaserProfile(1.9F, 2.0F, 1.6F);

    public boolean explodes() {
        return blastRadius > 0.0F;
    }
}
