package com.adamkali.dwm.render.portal;

import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Pure visibility decisions for the exterior doorway portal pass. Decides whether scheduling
 * the (expensive) off-screen portal render can contribute any pixels this frame.
 * <p>
 * Always under-culls when unsure: a stale composite for a frame is cheaper than a missing one.
 */
public final class PortalVisibility {
    /** Camera exactly on the door plane still counts as visible. */
    static final double PLANE_EPSILON = 1.0e-4;

    /**
     * Half-extent (blocks) of the box around the door center tested against the frustum.
     * Generous enough for the aperture of every chameleon variant plus swung door leaves.
     */
    static final double HORIZONTAL_RADIUS = 2.0;
    static final double VERTICAL_RADIUS = 2.5;

    private PortalVisibility() {
    }

    /** True when the camera is on the outward side of the door plane (or on it). */
    public static boolean isCameraInFrontOfDoorPlane(Vec3 cameraPos, Vec3 doorCenterWorld, Direction outward) {
        if (cameraPos == null || doorCenterWorld == null || outward == null) {
            return true;
        }
        double dot = (cameraPos.x - doorCenterWorld.x) * outward.getStepX()
                + (cameraPos.y - doorCenterWorld.y) * outward.getStepY()
                + (cameraPos.z - doorCenterWorld.z) * outward.getStepZ();
        return dot > -PLANE_EPSILON;
    }

    /** Conservative world-space box around the doorway, padded for door-leaf swing. */
    public static AABB apertureBounds(Vec3 doorCenterWorld) {
        return new AABB(
                doorCenterWorld.x - HORIZONTAL_RADIUS,
                doorCenterWorld.y - VERTICAL_RADIUS,
                doorCenterWorld.z - HORIZONTAL_RADIUS,
                doorCenterWorld.x + HORIZONTAL_RADIUS,
                doorCenterWorld.y + VERTICAL_RADIUS,
                doorCenterWorld.z + HORIZONTAL_RADIUS
        );
    }

    /**
     * True when the aperture box may intersect the view frustum. A {@code null} predicate means
     * the frustum is unknown, which counts as visible.
     */
    public static boolean isApertureInFrustum(Vec3 doorCenterWorld, Predicate<AABB> frustumVisible) {
        if (frustumVisible == null || doorCenterWorld == null) {
            return true;
        }
        return frustumVisible.test(apertureBounds(doorCenterWorld));
    }

    /** Whether the portal render should be scheduled this frame. */
    public static boolean shouldSchedule(
            Vec3 cameraPos,
            Vec3 doorCenterWorld,
            Direction outward,
            Predicate<AABB> frustumVisible
    ) {
        return isCameraInFrontOfDoorPlane(cameraPos, doorCenterWorld, outward)
                && isApertureInFrustum(doorCenterWorld, frustumVisible);
    }
}
