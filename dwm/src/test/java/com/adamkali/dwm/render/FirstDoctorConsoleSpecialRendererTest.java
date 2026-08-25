package com.adamkali.dwm.render;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstDoctorConsoleSpecialRendererTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void unbakedCodec_isIdentityType() {
        FirstDoctorConsoleSpecialRenderer.Unbaked unbaked = new FirstDoctorConsoleSpecialRenderer.Unbaked();
        assertSame(FirstDoctorConsoleSpecialRenderer.Unbaked.MAP_CODEC, unbaked.type());
    }

    @Test
    void emitExtents_matchesConsolePedestalAabb() {
        List<Vector3fc> corners = new ArrayList<>();
        FirstDoctorConsoleSpecialRenderer.emitExtents(corners::add);
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

        // Matches FirstDoctorConsoleBlock.COLLISION_SHAPE (~1.6×1.25×1.6).
        assertEquals(-0.3f, minX, EPSILON);
        assertEquals(0.0f, minY, EPSILON);
        assertEquals(-0.3f, minZ, EPSILON);
        assertEquals(1.3f, maxX, EPSILON);
        assertEquals(1.25f, maxY, EPSILON);
        assertEquals(1.3f, maxZ, EPSILON);

        assertEquals(1.6f, maxX - minX, EPSILON);
        assertEquals(1.25f, maxY - minY, EPSILON);
        assertEquals(1.6f, maxZ - minZ, EPSILON);
        assertTrue(maxY > 1.0f);
    }
}
