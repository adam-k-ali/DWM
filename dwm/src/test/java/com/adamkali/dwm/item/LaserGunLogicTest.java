package com.adamkali.dwm.item;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaserGunLogicTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void laserGunIsRegistered() {
        assertNotNull(DWMItems.LASER_GUN);
        assertTrue(DWMItems.LASER_GUN instanceof LaserGunItem);
    }

    @Test
    void tuningIsPositive() {
        assertEquals(20, LaserGunLogic.COOLDOWN_TICKS);
        assertTrue(LaserGunLogic.PLAYER_DAMAGE > 0.0F);
        assertTrue(LaserGunLogic.LASER_SPEED > 0.0F);
    }

    @Test
    void muzzleIsAheadOfAndSlightlyBelowTheEye() {
        Vec3 eye = new Vec3(10, 65, -4);
        Vec3 muzzle = LaserGunLogic.muzzlePosition(eye, new Vec3(0, 0, 1));
        assertEquals(10.0, muzzle.x, 1.0E-9);
        assertEquals(64.9, muzzle.y, 1.0E-9);
        assertEquals(-3.2, muzzle.z, 1.0E-9);
    }
}
