package com.adamkali.dwm.tardis.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalApertureTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void ofPixels_dividesBySixteen() {
        PortalAperture aperture = PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f);

        assertEquals(-5.0f / 16.0f, aperture.x0(), EPSILON);
        assertEquals(5.0f / 16.0f, aperture.x1(), EPSILON);
        assertEquals(1.0f / 16.0f, aperture.y0(), EPSILON);
        assertEquals(23.0f / 16.0f, aperture.y1(), EPSILON);
        assertEquals(-5.5f / 16.0f, aperture.z(), EPSILON);
        assertEquals((1.0f + 23.0f) / 32.0f, aperture.centerY(), EPSILON);
    }

    @Test
    void everyVariant_hasNonDegenerateAperture() {
        for (TardisChameleonVariant variant : TardisChameleonVariant.values()) {
            PortalAperture aperture = variant.getAperture();
            assertTrue(aperture.x0() < aperture.x1(), variant + " aperture x range");
            assertTrue(aperture.y0() < aperture.y1(), variant + " aperture y range");
        }
    }

    @Test
    void firstAndFifthDoctor_doorPlanesDiffer() {
        float firstZ = TardisChameleonVariant.FIRST_DOCTOR_BOX.getAperture().z();
        float fifthZ = TardisChameleonVariant.FIFTH_DOCTOR_BOX.getAperture().z();

        assertEquals(-5.5f / 16.0f, firstZ, EPSILON);
        assertEquals(-6.0f / 16.0f, fifthZ, EPSILON);
    }

    @Test
    void classicInteriorDoors_coversThreeByTwoOpening() {
        PortalAperture aperture = PortalAperture.CLASSIC_INTERIOR_DOORS;
        assertEquals(-1.0f, aperture.x0(), EPSILON);
        assertEquals(2.0f, aperture.x1(), EPSILON);
        assertEquals(0.0f, aperture.y0(), EPSILON);
        assertEquals(2.0f, aperture.y1(), EPSILON);
    }
}
