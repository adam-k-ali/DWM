package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekLaserProfileTest {
    @Test
    void standardBoltDoesNotExplode() {
        assertFalse(DalekLaserProfile.STANDARD.explodes());
    }

    @Test
    void heavyBoltExplodesAndOutclassesStandard() {
        assertTrue(DalekLaserProfile.HEAVY.explodes());
        assertTrue(DalekLaserProfile.HEAVY.speed() > DalekLaserProfile.STANDARD.speed());
        assertTrue(DalekLaserProfile.HEAVY.visualScale() > DalekLaserProfile.STANDARD.visualScale());
    }
}
