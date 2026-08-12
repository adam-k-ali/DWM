package com.adamkali.dwm.render.soto.ghost;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Pure hitch-camera visibility tests for ghost chunk columns.
 * Prefers under-culling: only drops columns clearly behind or far outside a wide frustum.
 *
 * <p>Portal stream {@code chunkX/Z} are world section coords, but hitch eye/view and mesh
 * vertices are footprint-relative — convert with {@code footprintOrigin} before testing.
 */
public final class SotoGhostHitchCull {
    /** Matches {@link SotoGhostExterior} vertical range (relative Y after origin subtract). */
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    /**
     * View-space +Z behind the camera (JOML lookAt looks down -Z). Chunks with all corners
     * farther behind than this margin are culled.
     */
    static final float BEHIND_MARGIN = 8.0f;

    /**
     * Generous |x|/(-z) limit (~68° half-angle with slack). Vertical FOV is not applied:
     * ghost columns span full world height, so Y-corner tests would over-cull.
     */
    static final float WIDE_FRUSTUM_TAN = 2.5f;

    private SotoGhostHitchCull() {
    }

    /**
     * Returns true when the chunk column may contribute pixels for the hitch camera.
     * Null hitch inputs keep the chunk (under-cull / safe fallback).
     *
     * @param footprintOrigin world min corner of the ghost footprint; null treated as {@link BlockPos#ZERO}
     */
    public static boolean isChunkVisibleToHitch(
            int chunkX,
            int chunkZ,
            BlockPos footprintOrigin,
            Vec3 eyeRelative,
            Vec3 lookDirection,
            Matrix4f viewMatrix
    ) {
        if (eyeRelative == null || lookDirection == null) {
            return true;
        }
        float[] aabb = relativeChunkAabb(chunkX, chunkZ, footprintOrigin);
        if (viewMatrix != null) {
            return isChunkVisibleInView(aabb[0], aabb[1], aabb[2], aabb[3], viewMatrix);
        }
        return isChunkVisibleInForwardHalfSpace(aabb[0], aabb[1], aabb[2], aabb[3], eyeRelative, lookDirection);
    }

    /**
     * World chunk column → footprint-relative XZ AABB: {@code [minX, maxX, minZ, maxZ]}.
     */
    static float[] relativeChunkAabb(int chunkX, int chunkZ, BlockPos footprintOrigin) {
        int originX = footprintOrigin == null ? 0 : footprintOrigin.getX();
        int originZ = footprintOrigin == null ? 0 : footprintOrigin.getZ();
        float minX = (chunkX << 4) - originX;
        float minZ = (chunkZ << 4) - originZ;
        return new float[]{minX, minX + 16.0f, minZ, minZ + 16.0f};
    }

    static boolean isChunkVisibleInView(
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            Matrix4f viewMatrix
    ) {
        float minY = MIN_Y;
        float maxY = MAX_Y;

        boolean anyInFront = false;
        boolean anyInWideFrustum = false;
        Vector3f scratch = new Vector3f();
        for (int xi = 0; xi < 2; xi++) {
            float x = xi == 0 ? minX : maxX;
            for (int yi = 0; yi < 2; yi++) {
                float y = yi == 0 ? minY : maxY;
                for (int zi = 0; zi < 2; zi++) {
                    float z = zi == 0 ? minZ : maxZ;
                    viewMatrix.transformPosition(x, y, z, scratch);
                    if (scratch.z <= BEHIND_MARGIN) {
                        anyInFront = true;
                        float negZ = Math.max(-scratch.z, 0.1f);
                        if (Math.abs(scratch.x) <= negZ * WIDE_FRUSTUM_TAN) {
                            anyInWideFrustum = true;
                        }
                    }
                }
            }
        }
        return anyInFront && anyInWideFrustum;
    }

    /** Test helper using already-relative chunk section coords (origin = ZERO). */
    static boolean isChunkVisibleInView(int chunkX, int chunkZ, Matrix4f viewMatrix) {
        float[] aabb = relativeChunkAabb(chunkX, chunkZ, BlockPos.ZERO);
        return isChunkVisibleInView(aabb[0], aabb[1], aabb[2], aabb[3], viewMatrix);
    }

    static boolean isChunkVisibleInForwardHalfSpace(
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            Vec3 eyeRelative,
            Vec3 lookDirection
    ) {
        Vec3 look = lookDirection.normalize();

        double maxDot = Double.NEGATIVE_INFINITY;
        for (int xi = 0; xi < 2; xi++) {
            double x = xi == 0 ? minX : maxX;
            for (int yi = 0; yi < 2; yi++) {
                double y = yi == 0 ? MIN_Y : MAX_Y;
                for (int zi = 0; zi < 2; zi++) {
                    double z = zi == 0 ? minZ : maxZ;
                    double dx = x - eyeRelative.x;
                    double dy = y - eyeRelative.y;
                    double dz = z - eyeRelative.z;
                    double dot = dx * look.x + dy * look.y + dz * look.z;
                    if (dot > maxDot) {
                        maxDot = dot;
                    }
                }
            }
        }
        // Culled only when the entire AABB is clearly behind the hitch plane.
        return maxDot >= -BEHIND_MARGIN;
    }

    /** Test helper using already-relative chunk section coords (origin = ZERO). */
    static boolean isChunkVisibleInForwardHalfSpace(
            int chunkX,
            int chunkZ,
            Vec3 eyeRelative,
            Vec3 lookDirection
    ) {
        float[] aabb = relativeChunkAabb(chunkX, chunkZ, BlockPos.ZERO);
        return isChunkVisibleInForwardHalfSpace(aabb[0], aabb[1], aabb[2], aabb[3], eyeRelative, lookDirection);
    }
}
