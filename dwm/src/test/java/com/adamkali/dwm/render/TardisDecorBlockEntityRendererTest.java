package com.adamkali.dwm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisDecorBlockEntityRendererTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void applyTransforms_centersOnBlock() {
        PoseStack pose = new PoseStack();
        TardisDecorBlockEntityRenderer.applyTransforms(pose, Direction.SOUTH);
        Matrix4f matrix = pose.last().pose();
        // Translation column: (0.5, 0, 0.5)
        assertEquals(0.5f, matrix.m30(), EPSILON);
        assertEquals(0.0f, matrix.m31(), EPSILON);
        assertEquals(0.5f, matrix.m32(), EPSILON);
    }

    @Test
    void applyTransforms_northAndEastDiffer() {
        PoseStack north = new PoseStack();
        PoseStack east = new PoseStack();
        TardisDecorBlockEntityRenderer.applyTransforms(north, Direction.NORTH);
        TardisDecorBlockEntityRenderer.applyTransforms(east, Direction.EAST);
        assertFalse(matricesEqual(north.last().pose(), east.last().pose()));
    }

    @Test
    void applyTransforms_isDeterministic() {
        PoseStack a = new PoseStack();
        PoseStack b = new PoseStack();
        TardisDecorBlockEntityRenderer.applyTransforms(a, Direction.WEST);
        TardisDecorBlockEntityRenderer.applyTransforms(b, Direction.WEST);
        assertTrue(matricesEqual(a.last().pose(), b.last().pose()));
    }

    private static boolean matricesEqual(Matrix4f a, Matrix4f b) {
        for (int i = 0; i < 16; i++) {
            if (Math.abs(a.get(i / 4, i % 4) - b.get(i / 4, i % 4)) > EPSILON) {
                return false;
            }
        }
        return true;
    }
}
