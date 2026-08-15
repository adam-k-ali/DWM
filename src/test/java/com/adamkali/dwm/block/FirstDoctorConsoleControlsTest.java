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
    void panel3FourDials_haveDistinctCenters() {
        Direction facing = Direction.NORTH;
        AABB biome = FirstDoctorConsoleControls.biomeSelectorBox(facing);
        AABB waypoint = FirstDoctorConsoleControls.waypointSelectorBox(facing);
        AABB player = FirstDoctorConsoleControls.playerLocatorBox(facing);
        AABB planet = FirstDoctorConsoleControls.planetLocatorBox(facing);

        assertTrue(biome.minY > 0.5);
        assertTrue(waypoint.minY > 0.5);
        assertTrue(player.minY > 0.5);
        assertTrue(planet.minY > 0.5);
        assertTrue(
                FirstDoctorConsoleControls.planetLocatorDistanceFromCenter(facing) > 0.45,
                "planet locator should be out on Panel3 deck"
        );

        assertCentersDistinct(biome, waypoint);
        assertCentersDistinct(waypoint, player);
        assertCentersDistinct(player, planet);
        assertCentersDistinct(biome, planet);
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
    void lookRay_hitsWaypointAndPlayerLocatorFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB waypoint = FirstDoctorConsoleControls.waypointSelectorWorldBox(pos, facing);
        Vec3 waypointEye = new Vec3(waypoint.getCenter().x, waypoint.getCenter().y + 1.5, waypoint.getCenter().z);
        assertTrue(FirstDoctorConsoleControls.isWaypointSelectorLookHit(facing, pos, waypointEye, look, 5.0));

        AABB player = FirstDoctorConsoleControls.playerLocatorWorldBox(pos, facing);
        Vec3 playerEye = new Vec3(player.getCenter().x, player.getCenter().y + 1.5, player.getCenter().z);
        assertTrue(FirstDoctorConsoleControls.isPlayerLocatorLookHit(facing, pos, playerEye, look, 5.0));
    }

    @Test
    void lookRay_hitsChameleonCircuitFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.chameleonCircuitWorldBox(pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isChameleonCircuitLookHit(facing, pos, eye, look, 5.0));
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

    @Test
    void resolvePanel3LookHit_prefersClosestDial() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB waypoint = FirstDoctorConsoleControls.waypointSelectorWorldBox(pos, facing);
        Vec3 waypointEye = new Vec3(waypoint.getCenter().x, waypoint.getCenter().y + 1.5, waypoint.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.Panel3Control.WAYPOINT,
                FirstDoctorConsoleControls.resolvePanel3LookHit(facing, pos, waypointEye, look, 5.0)
        );

        AABB player = FirstDoctorConsoleControls.playerLocatorWorldBox(pos, facing);
        Vec3 playerEye = new Vec3(player.getCenter().x, player.getCenter().y + 1.5, player.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.Panel3Control.PLAYER,
                FirstDoctorConsoleControls.resolvePanel3LookHit(facing, pos, playerEye, look, 5.0)
        );

        AABB planet = FirstDoctorConsoleControls.planetLocatorWorldBox(pos, facing);
        Vec3 planetEye = new Vec3(planet.getCenter().x, planet.getCenter().y + 1.5, planet.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.Panel3Control.PLANET,
                FirstDoctorConsoleControls.resolvePanel3LookHit(facing, pos, planetEye, look, 5.0)
        );
    }

    @Test
    void resolvePanel6LookHit_prefersCloserControl() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB chameleon = FirstDoctorConsoleControls.chameleonCircuitWorldBox(pos, facing);
        Vec3 chameleonEye = new Vec3(chameleon.getCenter().x, chameleon.getCenter().y + 1.5, chameleon.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.Panel6Control.CHAMELEON,
                FirstDoctorConsoleControls.resolvePanel6LookHit(facing, pos, chameleonEye, look, 5.0)
        );

        AABB lever = FirstDoctorConsoleControls.materialisationLeverWorldBox(pos, facing);
        Vec3 leverEye = new Vec3(lever.getCenter().x, lever.getCenter().y + 1.5, lever.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.Panel6Control.LEVER,
                FirstDoctorConsoleControls.resolvePanel6LookHit(facing, pos, leverEye, look, 5.0)
        );

        AABB fastReturn = FirstDoctorConsoleControls.fastReturnWorldBox(pos, facing);
        Vec3 fastReturnEye = new Vec3(fastReturn.getCenter().x, fastReturn.getCenter().y + 1.5, fastReturn.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.Panel6Control.FAST_RETURN,
                FirstDoctorConsoleControls.resolvePanel6LookHit(facing, pos, fastReturnEye, look, 5.0)
        );
    }

    @Test
    void fastReturnBox_sitsOnPanel6DeckAwayFromCenter() {
        AABB box = FirstDoctorConsoleControls.fastReturnBox(Direction.NORTH);
        assertTrue(box.minY > 0.4, "fast return should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 2.0, "fast return should stay near console top, was maxY=" + box.maxY);
        assertTrue(
                FirstDoctorConsoleControls.fastReturnDistanceFromCenter(Direction.NORTH) > 0.45,
                "fast return should be out on Panel6 deck, not at the time rotor"
        );
    }

    @Test
    void lookRay_hitsFastReturnFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.fastReturnWorldBox(pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.isFastReturnLookHit(facing, pos, eye, look, 5.0));
    }

    @Test
    void resolvePanel6LookHit_prefersFastReturnOverLeverWhenCloser() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB fastReturn = FirstDoctorConsoleControls.fastReturnWorldBox(pos, facing);
        Vec3 eye = new Vec3(fastReturn.getCenter().x, fastReturn.getCenter().y + 1.5, fastReturn.getCenter().z);
        Vec3 look = new Vec3(0, -1, 0);
        assertEquals(
                FirstDoctorConsoleControls.Panel6Control.FAST_RETURN,
                FirstDoctorConsoleControls.resolvePanel6LookHit(facing, pos, eye, look, 5.0)
        );
    }

    @Test
    void resolveLookTarget_prefersClosestControl() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB waypoint = FirstDoctorConsoleControls.waypointSelectorWorldBox(pos, facing);
        Vec3 waypointEye = new Vec3(waypoint.getCenter().x, waypoint.getCenter().y + 1.5, waypoint.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, waypointEye, look, 5.0)
        );

        AABB chameleon = FirstDoctorConsoleControls.chameleonCircuitWorldBox(pos, facing);
        Vec3 chameleonEye = new Vec3(chameleon.getCenter().x, chameleon.getCenter().y + 1.5, chameleon.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, chameleonEye, look, 5.0)
        );

        AABB fastReturn = FirstDoctorConsoleControls.fastReturnWorldBox(pos, facing);
        Vec3 fastReturnEye = new Vec3(fastReturn.getCenter().x, fastReturn.getCenter().y + 1.5, fastReturn.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, fastReturnEye, look, 5.0)
        );
    }

    private static void assertCentersDistinct(AABB a, AABB b) {
        double dx = a.getCenter().x - b.getCenter().x;
        double dz = a.getCenter().z - b.getCenter().z;
        assertTrue(Math.hypot(dx, dz) > 0.1, "Panel3 dials should not share the same center");
    }
}
