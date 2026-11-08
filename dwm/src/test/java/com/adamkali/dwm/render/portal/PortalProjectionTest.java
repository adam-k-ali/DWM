package com.adamkali.dwm.render.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

class PortalProjectionTest {
    private static final float EPS = 1.0e-5f;

    @Test
    void focalLengthsComeFromFixedFovAndFboAspect() {
        Matrix4f m = PortalProjection.matrix(1920, 1080, 512f, false, new Matrix4f());
        float tanHalf = (float) Math.tan(Math.toRadians(PortalProjection.FOV_DEGREES) / 2.0);
        float aspect = 1920f / 1080f;

        assertEquals(1.0f / tanHalf, m.m11(), EPS);
        assertEquals(1.0f / (aspect * tanHalf), m.m00(), EPS);
    }

    @Test
    void sameFboSizeAlwaysGivesSameMatrix() {
        Matrix4f a = PortalProjection.matrix(854, 480, 256f, false, new Matrix4f());
        Matrix4f b = PortalProjection.matrix(854, 480, 256f, false, new Matrix4f());

        assertEquals(a, b);
    }

    @Test
    void verticalZoomIsIndependentOfAspect() {
        Matrix4f wide = PortalProjection.matrix(1920, 1080, 512f, false, new Matrix4f());
        Matrix4f narrow = PortalProjection.matrix(800, 800, 512f, false, new Matrix4f());

        assertEquals(wide.m11(), narrow.m11(), EPS);
        assertNotEquals(wide.m00(), narrow.m00(), EPS);
    }

    @Test
    void zeroHeightDoesNotProduceNonFiniteMatrix() {
        Matrix4f m = PortalProjection.matrix(100, 0, 512f, false, new Matrix4f());

        assertEquals(true, Float.isFinite(m.m00()));
    }

    @Test
    void usesReverseZLikeVanilla() {
        float far = 512f;
        Matrix4f m = PortalProjection.matrix(1920, 1080, far, false, new Matrix4f());
        org.joml.Vector4f nearPt = new org.joml.Vector4f(0, 0, -PortalProjection.Z_NEAR, 1).mul(m);
        org.joml.Vector4f farPt = new org.joml.Vector4f(0, 0, -far, 1).mul(m);

        assertEquals(1.0f, nearPt.z / nearPt.w, 1.0e-3f);
        assertEquals(-1.0f, farPt.z / farPt.w, 1.0e-3f);
    }

    @Test
    void farPlaneMatchesVanillaCameraFormula() {
        assertEquals(12 * 16 * 4.0f, PortalProjection.farPlane(12, 10), EPS);
        assertEquals(128 * 16, PortalProjection.farPlane(2, 128), EPS);
    }
}
