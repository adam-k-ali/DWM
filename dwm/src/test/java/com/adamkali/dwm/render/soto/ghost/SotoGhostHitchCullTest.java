package com.adamkali.dwm.render.soto.ghost;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import static org.junit.jupiter.api.Assertions.*;

class SotoGhostHitchCullTest {

    @Test
    void forwardHalfSpace_keepsChunkInFront() {
        Vec3 eye = new Vec3(8.0, 64.0, 8.0);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        assertTrue(SotoGhostHitchCull.isChunkVisibleInForwardHalfSpace(0, 2, eye, look));
    }

    @Test
    void forwardHalfSpace_cullsChunkClearlyBehind() {
        Vec3 eye = new Vec3(8.0, 64.0, 8.0);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        assertFalse(SotoGhostHitchCull.isChunkVisibleInForwardHalfSpace(0, -3, eye, look));
    }

    @Test
    void forwardHalfSpace_keepsNearPlaneChunkWithMargin() {
        Vec3 eye = new Vec3(8.0, 64.0, 8.0);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        // Chunk immediately behind the eye but within BEHIND_MARGIN should stay.
        assertTrue(SotoGhostHitchCull.isChunkVisibleInForwardHalfSpace(0, 0, eye, look));
    }

    @Test
    void viewFrustum_keepsChunkAlongLookAxis() {
        Vec3 eye = new Vec3(8.0, 64.0, 8.0);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        Matrix4f view = new Matrix4f().lookAt(
                (float) eye.x, (float) eye.y, (float) eye.z,
                (float) (eye.x + look.x), (float) (eye.y + look.y), (float) (eye.z + look.z),
                0.0f, 1.0f, 0.0f
        );
        assertTrue(SotoGhostHitchCull.isChunkVisibleInView(0, 2, view));
        assertTrue(SotoGhostHitchCull.isChunkVisibleToHitch(0, 2, BlockPos.ZERO, eye, look, view));
    }

    @Test
    void viewFrustum_cullsChunkBehindCamera() {
        Vec3 eye = new Vec3(8.0, 64.0, 8.0);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        Matrix4f view = new Matrix4f().lookAt(
                (float) eye.x, (float) eye.y, (float) eye.z,
                (float) (eye.x + look.x), (float) (eye.y + look.y), (float) (eye.z + look.z),
                0.0f, 1.0f, 0.0f
        );
        assertFalse(SotoGhostHitchCull.isChunkVisibleInView(0, -4, view));
        assertFalse(SotoGhostHitchCull.isChunkVisibleToHitch(0, -4, BlockPos.ZERO, eye, look, view));
    }

    @Test
    void nullHitchInputs_underCullKeepChunk() {
        assertTrue(SotoGhostHitchCull.isChunkVisibleToHitch(1, 1, BlockPos.ZERO, null, null, null));
        assertTrue(SotoGhostHitchCull.isChunkVisibleToHitch(
                1, 1, BlockPos.ZERO, new Vec3(0, 0, 0), null, null
        ));
    }

    @Test
    void worldChunkKeys_convertViaFootprintOrigin_beforeCull() {
        // Matches production: world section keys + relative hitch eye.
        BlockPos origin = new BlockPos(-5, 63, -101);
        Vec3 eye = new Vec3(5.5, 1.75, 6.5);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        Matrix4f view = new Matrix4f().lookAt(
                (float) eye.x, (float) eye.y, (float) eye.z,
                (float) (eye.x + look.x), (float) (eye.y + look.y), (float) (eye.z + look.z),
                0.0f, 1.0f, 0.0f
        );

        // World chunk (0, -6) → relative Z ≈ [5, 21], in front of the door hitch.
        float[] aabb = SotoGhostHitchCull.relativeChunkAabb(0, -6, origin);
        assertEquals(5.0f, aabb[0], 1e-4);
        assertEquals(21.0f, aabb[1], 1e-4);
        assertEquals(5.0f, aabb[2], 1e-4);
        assertEquals(21.0f, aabb[3], 1e-4);

        assertTrue(SotoGhostHitchCull.isChunkVisibleToHitch(0, -6, origin, eye, look, view));
        // Without footprint conversion the same world key is far behind the relative camera.
        assertFalse(SotoGhostHitchCull.isChunkVisibleToHitch(0, -6, BlockPos.ZERO, eye, look, view));
    }
}
