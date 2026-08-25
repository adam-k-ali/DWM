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
    void consolePanel_encodesSixPurposesAndYaws() {
        assertEquals(1, FirstDoctorConsoleControls.ConsolePanel.ENVIRONMENT.index());
        assertEquals("Environment", FirstDoctorConsoleControls.ConsolePanel.ENVIRONMENT.purpose());
        assertEquals(0.0F, FirstDoctorConsoleControls.PANEL1_YAW_RAD, EPSILON);
        assertEquals(1.047198F, FirstDoctorConsoleControls.PANEL2_YAW_RAD, EPSILON);
        assertEquals(2.094395F, FirstDoctorConsoleControls.PANEL3_YAW_RAD, EPSILON);
        assertEquals(-3.141593F, FirstDoctorConsoleControls.PANEL4_YAW_RAD, EPSILON);
        assertEquals(-2.094395F, FirstDoctorConsoleControls.PANEL5_YAW_RAD, EPSILON);
        assertEquals(-1.047198F, FirstDoctorConsoleControls.PANEL6_YAW_RAD, EPSILON);
        assertEquals(
                FirstDoctorConsoleControls.ConsolePanel.NAVIGATION.yawRad(),
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                EPSILON
        );
        assertEquals(
                FirstDoctorConsoleControls.ConsolePanel.HELM.yawRad(),
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                EPSILON
        );
    }

    @Test
    void threeRowMounts_topIsCloserToRotorThanBottom() {
        assertEquals(9.081F, FirstDoctorConsoleControls.TOP_MOUNT_Y_PX, EPSILON);
        assertEquals(7.839F, FirstDoctorConsoleControls.TOP_MOUNT_Z_PX, EPSILON);
        assertEquals(8.081F, FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX, EPSILON);
        assertEquals(2.339F, FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX, EPSILON);
        assertEquals(7.081F, FirstDoctorConsoleControls.BOTTOM_MOUNT_Y_PX, EPSILON);
        assertEquals(-3.661F, FirstDoctorConsoleControls.BOTTOM_MOUNT_Z_PX, EPSILON);
        assertTrue(FirstDoctorConsoleControls.TOP_MOUNT_Z_PX > FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX);
        assertTrue(FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX > FirstDoctorConsoleControls.BOTTOM_MOUNT_Z_PX);
    }

    @Test
    void catalog_coversEveryInteractiveLookTarget() {
        for (FirstDoctorConsoleControls.LookTarget target :
                FirstDoctorConsoleControls.LookTarget.interactiveValues()) {
            assertNotNull(FirstDoctorConsoleControls.spec(target), target.name());
        }
        assertNull(FirstDoctorConsoleControls.spec(FirstDoctorConsoleControls.LookTarget.NONE));
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.interactiveValues().length,
                FirstDoctorConsoleControls.specs().size()
        );
    }

    @Test
    void biomeSelectorBox_sitsOnPanelDeckAwayFromCenter() {
        AABB box = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, Direction.NORTH);
        assertTrue(box.minY > 0.5, "selector should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 1.7, "selector should stay near console top, was maxY=" + box.maxY);
        assertTrue(box.getXsize() > 0.08);
        assertTrue(box.getZsize() > 0.08);
        assertTrue(
                FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, Direction.NORTH) > 0.45,
                "selector should be out on Panel3 deck, not at the time rotor"
        );
    }

    @Test
    void boxFor_acceptsCenterRejectsOrigin() {
        Direction facing = Direction.SOUTH;
        AABB box = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, facing);
        Vec3 center = box.getCenter();
        assertTrue(box.contains(center));
        assertFalse(box.contains(Vec3.ZERO));
        assertFalse(box.contains(new Vec3(0.5, 0.1, 0.5)));
    }

    @Test
    void lookRay_hitsSelectorFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, facing, pos, eye, look, 5.0));

        Vec3 missEye = new Vec3(-2.0, 2.0, -2.0);
        Vec3 missLook = new Vec3(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void facingRotation_movesSelectorHorizontally() {
        AABB north = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, Direction.NORTH);
        AABB east = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, Direction.EAST);
        assertNotEquals(north.getCenter().x, east.getCenter().x, EPSILON);
        assertEquals(north.getCenter().y, east.getCenter().y, EPSILON);
    }

    @Test
    void controlLocalToBlockLocal_panel3North_isOffsetFromCenter() {
        ConsoleControlSpec biome = FirstDoctorConsoleControls.spec(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR);
        assertNotNull(biome);
        Vec3 p = FirstDoctorConsoleControls.controlLocalToBlockLocal(0, 1, 0, Direction.NORTH, biome);
        assertTrue(Math.hypot(p.x - 0.5, p.z - 0.5) > 0.45);
        assertTrue(p.y > 0.5);
    }

    @Test
    void materialisationLeverBox_sitsOnPanel6DeckAwayFromCenter() {
        AABB box = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, Direction.NORTH);
        assertTrue(box.minY > 0.4, "lever should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 2.0, "lever should stay near console top, was maxY=" + box.maxY);
        assertTrue(
                FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, Direction.NORTH) > 0.45,
                "lever should be out on Panel6 deck, not at the time rotor"
        );
    }

    @Test
    void lookRay_hitsLeverFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, facing, pos, eye, look, 5.0));

        Vec3 missEye = new Vec3(-2.0, 2.0, -2.0);
        Vec3 missLook = new Vec3(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void facingRotation_movesLeverHorizontally() {
        AABB north = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, Direction.NORTH);
        AABB east = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, Direction.EAST);
        assertNotEquals(north.getCenter().x, east.getCenter().x, EPSILON);
        assertEquals(north.getCenter().y, east.getCenter().y, EPSILON);
    }

    @Test
    void leverAndSelector_areOnDifferentPanels() {
        AABB selector = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, Direction.NORTH);
        AABB lever = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, Direction.NORTH);
        double dx = selector.getCenter().x - lever.getCenter().x;
        double dz = selector.getCenter().z - lever.getCenter().z;
        assertTrue(Math.hypot(dx, dz) > 0.3, "Panel3 and Panel6 controls should not share the same center");
    }

    @Test
    void panel3FourDials_haveDistinctCenters() {
        Direction facing = Direction.NORTH;
        AABB biome = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, facing);
        AABB waypoint = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR, facing);
        AABB player = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.PLAYER_LOCATOR, facing);
        AABB planet = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR, facing);

        assertTrue(biome.minY > 0.5);
        assertTrue(waypoint.minY > 0.5);
        assertTrue(player.minY > 0.5);
        assertTrue(planet.minY > 0.5);
        assertTrue(
                FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR, facing) > 0.45,
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
        AABB box = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR, pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR, facing, pos, eye, look, 5.0));

        Vec3 missEye = new Vec3(-2.0, 2.0, -2.0);
        Vec3 missLook = new Vec3(0, -1, 0);
        assertFalse(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR, facing, pos, missEye, missLook, 5.0));
    }

    @Test
    void lookRay_hitsWaypointAndPlayerLocatorFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB waypoint = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR, pos, facing);
        Vec3 waypointEye = new Vec3(waypoint.getCenter().x, waypoint.getCenter().y + 1.5, waypoint.getCenter().z);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR, facing, pos, waypointEye, look, 5.0));

        AABB player = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.PLAYER_LOCATOR, pos, facing);
        Vec3 playerEye = new Vec3(player.getCenter().x, player.getCenter().y + 1.5, player.getCenter().z);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.PLAYER_LOCATOR, facing, pos, playerEye, look, 5.0));
    }

    @Test
    void lookRay_hitsChameleonCircuitFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT, pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT, facing, pos, eye, look, 5.0));
    }

    @Test
    void resolveLookTarget_prefersClosestDialOnPanel3() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB biome = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR, pos, facing);
        Vec3 biomeEye = new Vec3(biome.getCenter().x, biome.getCenter().y + 1.5, biome.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.BIOME_SELECTOR,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, biomeEye, look, 5.0)
        );

        AABB planet = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR, pos, facing);
        Vec3 planetEye = new Vec3(planet.getCenter().x, planet.getCenter().y + 1.5, planet.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.PLANET_LOCATOR,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, planetEye, look, 5.0)
        );

        AABB waypoint = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR, pos, facing);
        Vec3 waypointEye = new Vec3(waypoint.getCenter().x, waypoint.getCenter().y + 1.5, waypoint.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, waypointEye, look, 5.0)
        );

        AABB player = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.PLAYER_LOCATOR, pos, facing);
        Vec3 playerEye = new Vec3(player.getCenter().x, player.getCenter().y + 1.5, player.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.PLAYER_LOCATOR,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, playerEye, look, 5.0)
        );
    }

    @Test
    void resolveLookTarget_prefersCloserPanel6Control() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB chameleon = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT, pos, facing);
        Vec3 chameleonEye = new Vec3(chameleon.getCenter().x, chameleon.getCenter().y + 1.5, chameleon.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, chameleonEye, look, 5.0)
        );

        AABB lever = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, pos, facing);
        Vec3 leverEye = new Vec3(lever.getCenter().x, lever.getCenter().y + 1.5, lever.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, leverEye, look, 5.0)
        );

        AABB fastReturn = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN, pos, facing);
        Vec3 fastReturnEye = new Vec3(
                fastReturn.getCenter().x, fastReturn.getCenter().y + 1.5, fastReturn.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, fastReturnEye, look, 5.0)
        );

        AABB stabilisers = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.STABILISERS, pos, facing);
        Vec3 stabilisersEye = new Vec3(
                stabilisers.getCenter().x, stabilisers.getCenter().y + 1.5, stabilisers.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.STABILISERS,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, stabilisersEye, look, 5.0)
        );
    }

    @Test
    void stabilisersBox_sitsOnPanel6BottomRowAwayFromRotor() {
        AABB box = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.STABILISERS, Direction.NORTH);
        assertTrue(box.minY > 0.3, "stabilisers should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 2.0, "stabilisers should stay near console top, was maxY=" + box.maxY);
        assertTrue(
                FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.STABILISERS, Direction.NORTH) > 0.45,
                "stabilisers should be out on Panel6 deck, not at the time rotor"
        );
        assertTrue(
                FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.STABILISERS, Direction.NORTH)
                        > FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, Direction.NORTH),
                "bottom-row stabilisers should be farther from center than the middle-row lever"
        );
    }

    @Test
    void resolveLookTarget_prefersStabilisersOverLeverFromOuterDeck() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB stabilisers = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.STABILISERS, pos, facing);
        Vec3 eye = new Vec3(stabilisers.getCenter().x, stabilisers.getCenter().y + 1.5, stabilisers.getCenter().z);
        Vec3 look = new Vec3(0, -1, 0);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.STABILISERS,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, eye, look, 5.0)
        );
    }

    @Test
    void lookRay_hitsStabilisersFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.STABILISERS, pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.STABILISERS, facing, pos, eye, look, 5.0));
    }

    @Test
    void resolveLookTarget_includesStabilisers() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB stabilisers = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.STABILISERS, pos, facing);
        Vec3 eye = new Vec3(stabilisers.getCenter().x, stabilisers.getCenter().y + 1.5, stabilisers.getCenter().z);
        Vec3 look = new Vec3(0, -1, 0);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.STABILISERS,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, eye, look, 5.0)
        );
    }

    @Test
    void fastReturnBox_sitsOnPanel6DeckAwayFromCenter() {
        AABB box = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN, Direction.NORTH);
        assertTrue(box.minY > 0.4, "fast return should sit on panel deck, was minY=" + box.minY);
        assertTrue(box.maxY < 2.0, "fast return should stay near console top, was maxY=" + box.maxY);
        assertTrue(
                FirstDoctorConsoleControls.distanceFromCenter(
                        FirstDoctorConsoleControls.LookTarget.FAST_RETURN, Direction.NORTH) > 0.45,
                "fast return should be out on Panel6 deck, not at the time rotor"
        );
    }

    @Test
    void lookRay_hitsFastReturnFromAbove() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB box = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN, pos, facing);
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(center.x, center.y + 1.5, center.z);
        Vec3 look = new Vec3(0, -1, 0);
        assertTrue(FirstDoctorConsoleControls.lookHits(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN, facing, pos, eye, look, 5.0));
    }

    @Test
    void resolveLookTarget_prefersFastReturnOverLeverWhenCloser() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        AABB fastReturn = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN, pos, facing);
        Vec3 eye = new Vec3(fastReturn.getCenter().x, fastReturn.getCenter().y + 1.5, fastReturn.getCenter().z);
        Vec3 look = new Vec3(0, -1, 0);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, eye, look, 5.0)
        );
    }

    @Test
    void resolveLookTarget_prefersClosestControl() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        Vec3 look = new Vec3(0, -1, 0);

        AABB waypoint = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR, pos, facing);
        Vec3 waypointEye = new Vec3(waypoint.getCenter().x, waypoint.getCenter().y + 1.5, waypoint.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.WAYPOINT_SELECTOR,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, waypointEye, look, 5.0)
        );

        AABB chameleon = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT, pos, facing);
        Vec3 chameleonEye = new Vec3(chameleon.getCenter().x, chameleon.getCenter().y + 1.5, chameleon.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, chameleonEye, look, 5.0)
        );

        AABB fastReturn = FirstDoctorConsoleControls.worldBoxForTarget(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN, pos, facing);
        Vec3 fastReturnEye = new Vec3(
                fastReturn.getCenter().x, fastReturn.getCenter().y + 1.5, fastReturn.getCenter().z);
        assertEquals(
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN,
                FirstDoctorConsoleControls.resolveLookTarget(facing, pos, fastReturnEye, look, 5.0)
        );
    }

    @Test
    void panel6InteractionPoses_doNotOverlap() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        FirstDoctorConsoleControls.LookTarget[] panel6 = {
                FirstDoctorConsoleControls.LookTarget.CHAMELEON_CIRCUIT,
                FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER,
                FirstDoctorConsoleControls.LookTarget.FAST_RETURN,
                FirstDoctorConsoleControls.LookTarget.STABILISERS
        };
        for (int i = 0; i < panel6.length; i++) {
            FirstDoctorConsoleControls.InteractionPose a =
                    FirstDoctorConsoleControls.interactionPose(panel6[i], pos, facing);
            assertNotNull(a);
            assertTrue(a.height() > 0.05f, panel6[i] + " height");
            assertTrue(a.width() > 0.05f, panel6[i] + " width");
            for (int j = i + 1; j < panel6.length; j++) {
                FirstDoctorConsoleControls.InteractionPose b =
                        FirstDoctorConsoleControls.interactionPose(panel6[j], pos, facing);
                assertNotNull(b);
                assertFalse(
                        a.aabb().intersects(b.aabb()),
                        panel6[i] + " must not overlap " + panel6[j]
                );
            }
        }
    }

    @Test
    void leverAndStabilisersInteractionPoses_sitOnOuterDeck() {
        Direction facing = Direction.NORTH;
        BlockPos pos = BlockPos.ZERO;
        FirstDoctorConsoleControls.InteractionPose lever =
                FirstDoctorConsoleControls.interactionPose(
                        FirstDoctorConsoleControls.LookTarget.MATERIALISATION_LEVER, pos, facing);
        FirstDoctorConsoleControls.InteractionPose stabilisers =
                FirstDoctorConsoleControls.interactionPose(
                        FirstDoctorConsoleControls.LookTarget.STABILISERS, pos, facing);
        assertNotNull(lever);
        assertNotNull(stabilisers);
        assertTrue(lever.position().y > 0.3);
        assertTrue(stabilisers.position().y > 0.3);
        double leverDist = Math.hypot(lever.position().x - 0.5, lever.position().z - 0.5);
        double stabDist = Math.hypot(stabilisers.position().x - 0.5, stabilisers.position().z - 0.5);
        assertTrue(leverDist > 0.45, "lever should be out on Panel6 deck");
        assertTrue(stabDist > leverDist, "stabilisers should be farther out than lever");
    }

    @Test
    void dwm033Boxes_sitOnAssignedPanelsAndStayOffRotor() {
        Direction facing = Direction.NORTH;
        FirstDoctorConsoleControls.LookTarget[] targets = {
                FirstDoctorConsoleControls.LookTarget.OXYGEN_READER,
                FirstDoctorConsoleControls.LookTarget.PRESSURE_READER,
                FirstDoctorConsoleControls.LookTarget.TEMPERATURE_READER,
                FirstDoctorConsoleControls.LookTarget.RADIATION_READER,
                FirstDoctorConsoleControls.LookTarget.REFUELER,
                FirstDoctorConsoleControls.LookTarget.TELEPATHIC_CIRCUIT,
                FirstDoctorConsoleControls.LookTarget.CLOAK,
                FirstDoctorConsoleControls.LookTarget.DOOR_LOCK,
                FirstDoctorConsoleControls.LookTarget.COORDINATE_LOCK_X,
                FirstDoctorConsoleControls.LookTarget.COORDINATE_LOCK_Y,
                FirstDoctorConsoleControls.LookTarget.COORDINATE_LOCK_Z
        };
        for (FirstDoctorConsoleControls.LookTarget target : targets) {
            double dist = FirstDoctorConsoleControls.distanceFromCenter(target, facing);
            assertTrue(dist > 0.45, target + " should sit on a panel deck, dist=" + dist);
            AABB box = FirstDoctorConsoleControls.boxFor(target, facing);
            assertFalse(box.contains(0.5, 1.0, 0.5), target + " must stay off the rotor");
        }

        double oxygen = FirstDoctorConsoleControls.distanceFromCenter(
                FirstDoctorConsoleControls.LookTarget.OXYGEN_READER, facing);
        double radiation = FirstDoctorConsoleControls.distanceFromCenter(
                FirstDoctorConsoleControls.LookTarget.RADIATION_READER, facing);
        assertTrue(radiation > oxygen, "radiation should sit on the outer row");

        AABB oxygenBox = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.OXYGEN_READER, facing);
        AABB pressureBox = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.PRESSURE_READER, facing);
        AABB temperatureBox = FirstDoctorConsoleControls.boxFor(
                FirstDoctorConsoleControls.LookTarget.TEMPERATURE_READER, facing);
        assertCentersDistinct(oxygenBox, pressureBox);
        assertCentersDistinct(pressureBox, temperatureBox);
    }

    private static void assertCentersDistinct(AABB a, AABB b) {
        double dx = a.getCenter().x - b.getCenter().x;
        double dz = a.getCenter().z - b.getCenter().z;
        assertTrue(Math.hypot(dx, dz) > 0.1, "Panel3 dials should not share the same center");
    }
}
