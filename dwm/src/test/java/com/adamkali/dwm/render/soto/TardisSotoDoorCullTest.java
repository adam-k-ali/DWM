package com.adamkali.dwm.render.soto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adamkali.dwm.render.portal.PortalVisibility;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TardisSotoDoorCullTest {
    private static final BlockPos ORIGIN = new BlockPos(4, 70, -3);
    private static final Direction[] HORIZONTALS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    @Test
    void doorCenterIsMiddleOfThreeWideBank() {
        for (Direction facing : HORIZONTALS) {
            Vec3 c = TardisSotoRenderer.doorCenter(ORIGIN, facing);
            Direction along = facing.getCounterClockWise();
            assertEquals(ORIGIN.getX() + 0.5 + along.getStepX(), c.x, 1e-9, facing.toString());
            assertEquals(ORIGIN.getY() + 1.0, c.y, 1e-9, facing.toString());
            assertEquals(ORIGIN.getZ() + 0.5 + along.getStepZ(), c.z, 1e-9, facing.toString());
        }
    }

    @Test
    void roomSideCameraSchedulesAndBehindWallIsCulled() {
        Predicate<AABB> inView = box -> true;
        for (Direction facing : HORIZONTALS) {
            Vec3 c = TardisSotoRenderer.doorCenter(ORIGIN, facing);
            Vec3 inRoom = c.add(facing.getStepX() * 4.0, 0.0, facing.getStepZ() * 4.0);
            Vec3 behind = c.add(facing.getStepX() * -4.0, 0.0, facing.getStepZ() * -4.0);
            assertTrue(PortalVisibility.shouldSchedule(inRoom, c, facing, inView), facing.toString());
            assertFalse(PortalVisibility.shouldSchedule(behind, c, facing, inView), facing.toString());
            assertFalse(PortalVisibility.shouldSchedule(inRoom, c, facing, box -> false), facing.toString());
        }
    }

    @Test
    void bankEdgeSlotsStayInsideApertureBounds() {
        for (Direction facing : HORIZONTALS) {
            Vec3 c = TardisSotoRenderer.doorCenter(ORIGIN, facing);
            AABB bounds = PortalVisibility.apertureBounds(c);
            Direction along = facing.getCounterClockWise();
            for (int slot = 0; slot < 3; slot++) {
                for (int half = 0; half < 2; half++) {
                    Vec3 cell = new Vec3(
                            ORIGIN.getX() + 0.5 + along.getStepX() * slot,
                            ORIGIN.getY() + 0.5 + half,
                            ORIGIN.getZ() + 0.5 + along.getStepZ() * slot);
                    assertTrue(bounds.contains(cell), facing + " slot " + slot + " half " + half);
                }
            }
        }
    }

    /** Guards against the predicate silently degrading to "always visible". */
    @Test
    void cameraRingCullsAboutHalfTheViewpoints() {
        Direction facing = Direction.SOUTH;
        Vec3 c = TardisSotoRenderer.doorCenter(ORIGIN, facing);
        int culled = 0;
        int total = 72;
        for (int i = 0; i < total; i++) {
            double a = Math.toRadians(i * (360.0 / total));
            Vec3 cam = c.add(Math.cos(a) * 6.0, 0.0, Math.sin(a) * 6.0);
            if (!PortalVisibility.shouldSchedule(cam, c, facing, box -> true)) {
                culled++;
            }
        }
        assertTrue(culled >= total / 2 - 2 && culled <= total / 2 + 2, "culled=" + culled);
    }
}
