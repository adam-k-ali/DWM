package com.adamkali.dwm.render.portal;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalObliqueClipTest {
    private static final float FAR = 512.0f;

    /** Door plane 5 blocks ahead (view space looks down -Z); keep everything beyond it. */
    private static final Vector4f PLANE_AHEAD = new Vector4f(0.0f, 0.0f, -1.0f, -5.0f);

    @Test
    void pointOnPlane_landsOnNearDepth() {
        Matrix4f clipped = PortalObliqueClip.apply(projection(true), PLANE_AHEAD, true);
        assertEquals(1.0f, ndcDepth(clipped, 0.0f, 0.0f, -5.0f), 1.0e-4f);
        assertEquals(1.0f, ndcDepth(clipped, 1.5f, -0.7f, -5.0f), 1.0e-4f);
    }

    @Test
    void pointsBeyondPlane_stayInDepthRange_andKeepOrder() {
        Matrix4f clipped = PortalObliqueClip.apply(projection(true), PLANE_AHEAD, true);
        float near = ndcDepth(clipped, 0.0f, 0.0f, -6.0f);
        float far = ndcDepth(clipped, 0.0f, 0.0f, -200.0f);
        assertInRange(near, 0.0f, 1.0f);
        assertInRange(far, 0.0f, 1.0f);
        assertTrue(near > far, "reverse-Z: nearer points have greater depth");
    }

    @Test
    void pointsBetweenEyeAndPlane_areClipped() {
        Matrix4f clipped = PortalObliqueClip.apply(projection(true), PLANE_AHEAD, true);
        assertTrue(ndcDepth(clipped, 0.0f, 0.0f, -3.0f) > 1.0f);
    }

    @Test
    void obliquePlane_clipsAlongThePlaneNotTheViewAxis() {
        // Plane tilted 45° about Y: only x + z <= -5 (beyond the door) is kept.
        Vector4f tilted = new Vector4f(-1.0f, 0.0f, -1.0f, -5.0f);
        Matrix4f clipped = PortalObliqueClip.apply(projection(true), tilted, true);
        assertEquals(1.0f, ndcDepth(clipped, -2.0f, 0.0f, -3.0f), 1.0e-3f);
        assertInRange(ndcDepth(clipped, -1.0f, 0.0f, -6.0f), 0.0f, 1.0f);
        assertTrue(ndcDepth(clipped, -2.0f, 0.0f, -2.0f) > 1.0f);
        assertTrue(ndcDepth(clipped, 1.0f, 0.0f, -5.0f) > 1.0f);
    }

    @Test
    void negativeOneToOneDepth_alsoMapsPlaneToNear() {
        Matrix4f clipped = PortalObliqueClip.apply(projection(false), PLANE_AHEAD, false);
        assertEquals(1.0f, ndcDepth(clipped, 0.0f, 0.0f, -5.0f), 1.0e-4f);
        assertInRange(ndcDepth(clipped, 0.0f, 0.0f, -100.0f), -1.0f, 1.0f);
    }

    @Test
    void cameraTooCloseToPlane_fallsBackToUnmodifiedProjection() {
        Matrix4f projection = projection(true);
        Vector4f grazing = new Vector4f(0.0f, 0.0f, -1.0f, -0.01f);
        assertTrue(PortalObliqueClip.apply(projection, grazing, true).equals(projection, 0.0f));
    }

    @Test
    void cameraOnVisibleSide_fallsBackToUnmodifiedProjection() {
        Matrix4f projection = projection(true);
        Vector4f behindViewer = new Vector4f(0.0f, 0.0f, -1.0f, 3.0f);
        assertTrue(PortalObliqueClip.apply(projection, behindViewer, true).equals(projection, 0.0f));
    }

    private static Matrix4f projection(boolean zZeroToOne) {
        return PortalProjection.matrix(1920, 1080, FAR, zZeroToOne, new Matrix4f());
    }

    private static float ndcDepth(Matrix4f projection, float x, float y, float z) {
        Vector4f clip = projection.transform(new Vector4f(x, y, z, 1.0f));
        return clip.z / clip.w;
    }

    private static void assertInRange(float value, float min, float max) {
        assertTrue(value >= min - 1.0e-5f && value <= max + 1.0e-5f, value + " not in [" + min + ", " + max + "]");
    }
}
