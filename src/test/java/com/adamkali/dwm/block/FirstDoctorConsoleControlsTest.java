package com.adamkali.dwm.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FirstDoctorConsoleControlsTest {
    private static final double EPSILON = 1e-3;

    @Test
    void biomeSelectorBox_sitsOnPanelDeckAwayFromCenter() {
        Box box = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        assertTrue(box.minY > 0.5, "selector should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 1.7, "selector should stay near console top, was maxY=" + box.maxY);
        assertTrue(box.getLengthX() > 0.2);
        assertTrue(box.getLengthZ() > 0.2);
        assertTrue(
                FirstDoctorConsoleControls.selectorDistanceFromCenter(Direction.NORTH) > 0.45,
                "selector should be out on Panel3 deck, not at the time rotor"
        );
    }

    @Test
    void isBiomeSelectorHit_acceptsCenterRejectsOrigin() {
        Direction facing = Direction.SOUTH;
        Box box = FirstDoctorConsoleControls.biomeSelectorBox(facing);
        Vec3d center = box.getCenter();
        assertTrue(FirstDoctorConsoleControls.isBiomeSelectorHit(facing, center));
        assertFalse(FirstDoctorConsoleControls.isBiomeSelectorHit(facing, Vec3d.ZERO));
        assertFalse(FirstDoctorConsoleControls.isBiomeSelectorHit(facing, new Vec3d(0.5, 0.1, 0.5)));
    }

    @Test
    void lookRay_hitsSelectorFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ORIGIN;
        Box box = FirstDoctorConsoleControls.biomeSelectorWorldBox(pos, facing);
        Vec3d center = box.getCenter();
        Vec3d eye = new Vec3d(center.x, center.y + 1.5, center.z);
        Vec3d look = new Vec3d(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, eye, look, 5.0));

        Vec3d missEye = new Vec3d(-2.0, 2.0, -2.0);
        Vec3d missLook = new Vec3d(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void facingRotation_movesSelectorHorizontally() {
        Box north = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        Box east = FirstDoctorConsoleControls.biomeSelectorBox(Direction.EAST);
        assertNotEquals(north.getCenter().x, east.getCenter().x, EPSILON);
        assertEquals(north.getCenter().y, east.getCenter().y, EPSILON);
    }

    @Test
    void selectorLocalToBlockLocal_panel3North_isOffsetFromCenter() {
        Vec3d p = FirstDoctorConsoleControls.selectorLocalToBlockLocal(0, 1, 0, Direction.NORTH);
        assertTrue(Math.hypot(p.x - 0.5, p.z - 0.5) > 0.45);
        assertTrue(p.y > 0.5);
    }

    @Test
    void materialisationLeverBox_sitsOnPanel6DeckAwayFromCenter() {
        Box box = FirstDoctorConsoleControls.materialisationLeverBox(Direction.NORTH);
        assertTrue(box.minY > 0.4, "lever should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 2.0, "lever should stay near console top, was maxY=" + box.maxY);
        assertTrue(
                FirstDoctorConsoleControls.leverDistanceFromCenter(Direction.NORTH) > 0.45,
                "lever should be out on Panel6 deck, not at the time rotor"
        );
    }

    @Test
    void lookRay_hitsLeverFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ORIGIN;
        Box box = FirstDoctorConsoleControls.materialisationLeverWorldBox(pos, facing);
        Vec3d center = box.getCenter();
        Vec3d eye = new Vec3d(center.x, center.y + 1.5, center.z);
        Vec3d look = new Vec3d(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, eye, look, 5.0));

        Vec3d missEye = new Vec3d(-2.0, 2.0, -2.0);
        Vec3d missLook = new Vec3d(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void facingRotation_movesLeverHorizontally() {
        Box north = FirstDoctorConsoleControls.materialisationLeverBox(Direction.NORTH);
        Box east = FirstDoctorConsoleControls.materialisationLeverBox(Direction.EAST);
        assertNotEquals(north.getCenter().x, east.getCenter().x, EPSILON);
        assertEquals(north.getCenter().y, east.getCenter().y, EPSILON);
    }

    @Test
    void leverAndSelector_areOnDifferentPanels() {
        Box selector = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        Box lever = FirstDoctorConsoleControls.materialisationLeverBox(Direction.NORTH);
        double dx = selector.getCenter().x - lever.getCenter().x;
        double dz = selector.getCenter().z - lever.getCenter().z;
        assertTrue(Math.hypot(dx, dz) > 0.3, "Panel3 and Panel6 controls should not share the same center");
    }

    @Test
    void planetLocatorBox_sitsBesideBiomeSelectorOnPanel3() {
        Box biome = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        Box planet = FirstDoctorConsoleControls.planetLocatorBox(Direction.NORTH);
        assertTrue(planet.minY > 0.5, "planet locator should sit on panel deck, was minY=" + planet.minY);
        assertTrue(
                FirstDoctorConsoleControls.planetLocatorDistanceFromCenter(Direction.NORTH) > 0.45,
                "planet locator should be out on Panel3 deck"
        );
        double dx = biome.getCenter().x - planet.getCenter().x;
        double dz = biome.getCenter().z - planet.getCenter().z;
        assertTrue(Math.hypot(dx, dz) > 0.15, "biome and planet dials should not share the same center");
        // Pads make AABBs slightly generous; centers must still be distinct enough to aim.
    }

    @Test
    void lookRay_hitsPlanetLocatorFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ORIGIN;
        Box box = FirstDoctorConsoleControls.planetLocatorWorldBox(pos, facing);
        Vec3d center = box.getCenter();
        Vec3d eye = new Vec3d(center.x, center.y + 1.5, center.z);
        Vec3d look = new Vec3d(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isPlanetLocatorLookHit(facing, pos, eye, look, 5.0));

        Vec3d missEye = new Vec3d(-2.0, 2.0, -2.0);
        Vec3d missLook = new Vec3d(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.isPlanetLocatorLookHit(facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void preferBiomeOverPlanet_picksCloserDial() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ORIGIN;
        Box biome = FirstDoctorConsoleControls.biomeSelectorWorldBox(pos, facing);
        Vec3d biomeCenter = biome.getCenter();
        Vec3d eye = new Vec3d(biomeCenter.x, biomeCenter.y + 1.5, biomeCenter.z);
        Vec3d look = new Vec3d(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.preferBiomeOverPlanet(facing, pos, eye, look, 5.0));

        Box planet = FirstDoctorConsoleControls.planetLocatorWorldBox(pos, facing);
        Vec3d planetCenter = planet.getCenter();
        Vec3d planetEye = new Vec3d(planetCenter.x, planetCenter.y + 1.5, planetCenter.z);
        assertFalse(FirstDoctorConsoleControls.preferBiomeOverPlanet(facing, pos, planetEye, look, 5.0));
    }
}
