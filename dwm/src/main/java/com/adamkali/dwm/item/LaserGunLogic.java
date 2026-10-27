package com.adamkali.dwm.item;

import net.minecraft.world.phys.Vec3;

/**
 * Pure tuning and geometry for the hand-held Dalek laser gun.
 */
public final class LaserGunLogic {
    public static final int COOLDOWN_TICKS = 20;
    public static final float PLAYER_DAMAGE = 4.0F;
    public static final float LASER_SPEED = 1.6F;
    private static final double MUZZLE_FORWARD = 0.8;
    private static final double MUZZLE_DOWN = 0.1;

    private LaserGunLogic() {
    }

    /** World position where the bolt appears: just ahead of the eye along the look vector. */
    public static Vec3 muzzlePosition(Vec3 eyePosition, Vec3 lookDirection) {
        return eyePosition.add(lookDirection.scale(MUZZLE_FORWARD)).add(0.0, -MUZZLE_DOWN, 0.0);
    }
}
