package com.adamkali.dwm.render;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TardisBlockSpecialRendererTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void unbakedCodec_isIdentityType() {
        TardisBlockSpecialRenderer.Unbaked unbaked = new TardisBlockSpecialRenderer.Unbaked();
        assertSame(TardisBlockSpecialRenderer.Unbaked.MAP_CODEC, unbaked.type());
    }

    @Test
    void emitExtents_matchesPoliceBoxVisualAabb() {
        List<Vector3fc> corners = new ArrayList<>();
        TardisBlockSpecialRenderer.emitExtents(corners::add);
        assertEquals(8, corners.size());

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (Vector3fc corner : corners) {
            minX = Math.min(minX, corner.x());
            minY = Math.min(minY, corner.y());
            minZ = Math.min(minZ, corner.z());
            maxX = Math.max(maxX, corner.x());
            maxY = Math.max(maxY, corner.y());
            maxZ = Math.max(maxZ, corner.z());
        }

        // Visual police box: 1×2×1 (collision is a unit cube; mesh is ~2 tall).
        assertEquals(0.0f, minX, EPSILON);
        assertEquals(0.0f, minY, EPSILON);
        assertEquals(0.0f, minZ, EPSILON);
        assertEquals(1.0f, maxX, EPSILON);
        assertEquals(2.0f, maxY, EPSILON);
        assertEquals(1.0f, maxZ, EPSILON);
    }
}
