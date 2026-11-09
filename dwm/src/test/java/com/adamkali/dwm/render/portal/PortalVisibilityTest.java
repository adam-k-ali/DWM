package com.adamkali.dwm.render.portal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PortalVisibilityTest {
    private static final Vec3 DOOR = new Vec3(10.5, 65.0, 20.5);
    private static final Direction[] HORIZONTALS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private static Vec3 offset(Direction dir, double distance) {
        return DOOR.add(dir.getStepX() * distance, 0.0, dir.getStepZ() * distance);
    }

    @Test
    void cameraInFrontOfPlaneIsVisibleForAllFacings() {
        for (Direction dir : HORIZONTALS) {
            assertTrue(PortalVisibility.isCameraInFrontOfDoorPlane(offset(dir, 5.0), DOOR, dir), dir.toString());
        }
    }

    @Test
    void cameraBehindPlaneIsCulledForAllFacings() {
        for (Direction dir : HORIZONTALS) {
            assertFalse(PortalVisibility.isCameraInFrontOfDoorPlane(offset(dir, -5.0), DOOR, dir), dir.toString());
        }
    }

    @Test
    void cameraOnPlaneCountsAsVisible() {
        for (Direction dir : HORIZONTALS) {
            Vec3 sideways = DOOR.add(dir.getStepZ() * 3.0, 2.0, dir.getStepX() * 3.0);
            assertTrue(PortalVisibility.isCameraInFrontOfDoorPlane(sideways, DOOR, dir), dir.toString());
        }
    }

    @Test
    void nullInputsUnderCull() {
        assertTrue(PortalVisibility.isCameraInFrontOfDoorPlane(null, DOOR, Direction.NORTH));
        assertTrue(PortalVisibility.isCameraInFrontOfDoorPlane(DOOR, null, Direction.NORTH));
        assertTrue(PortalVisibility.isCameraInFrontOfDoorPlane(DOOR, DOOR, null));
    }

    @Test
    void apertureOutsideFrustumIsCulled() {
        assertFalse(PortalVisibility.isApertureInFrustum(DOOR, box -> false));
    }

    @Test
    void apertureInsideFrustumIsVisible() {
        assertTrue(PortalVisibility.isApertureInFrustum(DOOR, box -> true));
    }

    @Test
    void unknownFrustumUnderCulls() {
        assertTrue(PortalVisibility.isApertureInFrustum(DOOR, null));
    }

    @Test
    void frustumTestUsesBoxPaddedAroundDoor() {
        AtomicReference<AABB> seen = new AtomicReference<>();
        Predicate<AABB> capture = box -> {
            seen.set(box);
            return true;
        };
        PortalVisibility.isApertureInFrustum(DOOR, capture);
        AABB box = seen.get();
        assertTrue(box.contains(DOOR));
        assertTrue(box.getXsize() >= 4.0 && box.getYsize() >= 4.0 && box.getZsize() >= 4.0);
    }

    @Test
    void shouldScheduleRequiresBothConditions() {
        Direction dir = Direction.SOUTH;
        Vec3 front = offset(dir, 4.0);
        Vec3 behind = offset(dir, -4.0);
        assertTrue(PortalVisibility.shouldSchedule(front, DOOR, dir, box -> true));
        assertFalse(PortalVisibility.shouldSchedule(behind, DOOR, dir, box -> true));
        assertFalse(PortalVisibility.shouldSchedule(front, DOOR, dir, box -> false));
        assertFalse(PortalVisibility.shouldSchedule(behind, DOOR, dir, box -> false));
    }
}
