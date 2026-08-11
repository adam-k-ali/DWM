package com.adamkali.dwm.block;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

class FirstDoctorConsoleControlsTest {
    private static final double EPSILON = 1e-3;

    @Test
    void biomeSelectorBox_sitsOnPanelDeckAwayFromCenter() {
        AABB box = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        assertTrue(box.minY > 0.5, "selector should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 1.7, "selector should stay near console top, was maxY=" + box.maxY);
        assertTrue(box.getXsize() > 0.2);
        assertTrue(box.getZsize() > 0.2);
        assertTrue(
                FirstDoctorConsoleControls.selectorDistanceFromCenter(Direction.NORTH) > 0.45,
                "selector should be out on Panel3 deck, not at the time rotor"
        );
    }

    @Test
    void isBiomeSelectorHit_acceptsCenterRejectsOrigin() {
        Direction facing = Direction.SOUTH;
        AABB box = FirstDoctorConsoleControls.biomeSelectorBox(facing);
        Vec3 center = box.getCenter();
        assertTrue(FirstDoctorConsoleControls.isBiomeSelectorHit(facing, center));
        assertFalse(FirstDoctorConsoleControls.isBiomeSelectorHit(facing, Vec3.ZERO));
        assertFalse(FirstDoctorConsoleControls.isBiomeSelectorHit(facing, new Vec3(0.5, 0.1, 0.5)));
    }

    @Test
    void lookRay_hitsSelectorFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.biomeSelectorWorldBox(pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, eye, look, 5.0));

        Vec3 missEye = new Vec3(-2.0, 2.0, -2.0);
        Vec3 missLook = new Vec3(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void facingRotation_movesSelectorHorizontally() {
        AABB north = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        AABB east = FirstDoctorConsoleControls.biomeSelectorBox(Direction.EAST);
        assertNotEquals(north.getCenter().x, east.getCenter().x, EPSILON);
        assertEquals(north.getCenter().y, east.getCenter().y, EPSILON);
    }

    @Test
    void selectorLocalToBlockLocal_panel3North_isOffsetFromCenter() {
        Vec3 p = FirstDoctorConsoleControls.selectorLocalToBlockLocal(0, 1, 0, Direction.NORTH);
        assertTrue(Math.hypot(p.x - 0.5, p.z - 0.5) > 0.45);
        assertTrue(p.y > 0.5);
    }

    @Test
    void materialisationLeverBox_sitsOnPanel6DeckAwayFromCenter() {
        AABB box = FirstDoctorConsoleControls.materialisationLeverBox(Direction.NORTH);
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
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.materialisationLeverWorldBox(pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, eye, look, 5.0));

        Vec3 missEye = new Vec3(-2.0, 2.0, -2.0);
        Vec3 missLook = new Vec3(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void facingRotation_movesLeverHorizontally() {
        AABB north = FirstDoctorConsoleControls.materialisationLeverBox(Direction.NORTH);
        AABB east = FirstDoctorConsoleControls.materialisationLeverBox(Direction.EAST);
        assertNotEquals(north.getCenter().x, east.getCenter().x, EPSILON);
        assertEquals(north.getCenter().y, east.getCenter().y, EPSILON);
    }

    @Test
    void leverAndSelector_areOnDifferentPanels() {
        AABB selector = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        AABB lever = FirstDoctorConsoleControls.materialisationLeverBox(Direction.NORTH);
        double dx = selector.getCenter().x - lever.getCenter().x;
        double dz = selector.getCenter().z - lever.getCenter().z;
        assertTrue(Math.hypot(dx, dz) > 0.3, "Panel3 and Panel6 controls should not share the same center");
    }

    @Test
    void planetLocatorBox_sitsBesideBiomeSelectorOnPanel3() {
        AABB biome = FirstDoctorConsoleControls.biomeSelectorBox(Direction.NORTH);
        AABB planet = FirstDoctorConsoleControls.planetLocatorBox(Direction.NORTH);
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
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.planetLocatorWorldBox(pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isPlanetLocatorLookHit(facing, pos, eye, look, 5.0));

        Vec3 missEye = new Vec3(-2.0, 2.0, -2.0);
        Vec3 missLook = new Vec3(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.isPlanetLocatorLookHit(facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void preferBiomeOverPlanet_picksCloserDial() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB biome = FirstDoctorConsoleControls.biomeSelectorWorldBox(pos, facing);
        Vec3 biomeCenter = biome.getCenter();
        Vec3 eye = new Vec3(biomeCenter.x, biomeCenter.y + 1.5, biomeCenter.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.preferBiomeOverPlanet(facing, pos, eye, look, 5.0));

        AABB planet = FirstDoctorConsoleControls.planetLocatorWorldBox(pos, facing);
        Vec3 planetCenter = planet.getCenter();
        Vec3 planetEye = new Vec3(planetCenter.x, planetCenter.y + 1.5, planetCenter.z);
        assertFalse(FirstDoctorConsoleControls.preferBiomeOverPlanet(facing, pos, planetEye, look, 5.0));
    }
}
