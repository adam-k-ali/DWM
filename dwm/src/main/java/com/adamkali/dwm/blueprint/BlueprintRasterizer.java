package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.Blueprint;
import com.adamkali.dwm.blueprint.model.BlueprintBlock;
import com.adamkali.dwm.blueprint.model.BlueprintLocation;
import com.adamkali.dwm.blueprint.model.BlueprintShape;
import com.adamkali.dwm.blueprint.model.CircleShape;
import com.adamkali.dwm.blueprint.model.DomeShape;
import com.adamkali.dwm.blueprint.model.PolygonShape;
import com.adamkali.dwm.blueprint.model.RectangleShape;
import com.adamkali.dwm.blueprint.model.SphereShape;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

/**
 * Pure voxelization of blueprint shapes into relative {@link BlockPos} → {@link BlueprintBlock} maps.
 * Later shapes overwrite earlier ones at the same voxel.
 */
public final class BlueprintRasterizer {
    /** Soft guard so a huge sphere cannot allocate an unbounded map during placement. */
    public static final int MAX_VOXELS = 250_000;

    private BlueprintRasterizer() {
    }

    /**
     * Rasterizes all shapes. Throws {@link BlueprintTooLargeException} if the result would exceed
     * {@link #MAX_VOXELS}.
     */
    public static Map<BlockPos, BlueprintBlock> rasterize(Blueprint blueprint) {
        Map<BlockPos, BlueprintBlock> voxels = new LinkedHashMap<>();
        for (BlueprintShape shape : blueprint.shapes()) {
            rasterizeShape(shape, voxels);
            if (voxels.size() > MAX_VOXELS) {
                throw new BlueprintTooLargeException(voxels.size());
            }
        }
        return voxels;
    }

    private static void rasterizeShape(BlueprintShape shape, Map<BlockPos, BlueprintBlock> out) {
        switch (shape) {
            case CircleShape c -> rasterizeCircle(c, out);
            case RectangleShape r -> rasterizeRectangle(r, out);
            case SphereShape s -> rasterizeSphere(s, out);
            case PolygonShape p -> rasterizePolygon(p, out);
            case DomeShape d -> rasterizeDome(d, out);
        }
    }

    private static void rasterizeCircle(CircleShape shape, Map<BlockPos, BlueprintBlock> out) {
        int cy = round(shape.origin().y());
        double ox = shape.origin().x();
        double oz = shape.origin().z();
        double r = shape.radius();
        double r2 = r * r;
        int minX = floor(ox - r);
        int maxX = ceil(ox + r);
        int minZ = floor(oz - r);
        int maxZ = ceil(oz + r);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x - ox;
                double dz = z - oz;
                double dist2 = dx * dx + dz * dz;
                if (dist2 > r2) {
                    continue;
                }
                if (shape.filled()) {
                    put(out, x, cy, z, shape.block());
                } else {
                    // Ring: inside radius, but at least one outward neighbor is outside.
                    if (isOutside(ox, oz, r2, x + 1, z)
                            || isOutside(ox, oz, r2, x - 1, z)
                            || isOutside(ox, oz, r2, x, z + 1)
                            || isOutside(ox, oz, r2, x, z - 1)) {
                        put(out, x, cy, z, shape.block());
                    }
                }
            }
        }
    }

    private static boolean isOutside(double ox, double oz, double r2, int x, int z) {
        double dx = x - ox;
        double dz = z - oz;
        return dx * dx + dz * dz > r2;
    }

    private static void rasterizeRectangle(RectangleShape shape, Map<BlockPos, BlueprintBlock> out) {
        int minX = Math.min(round(shape.from().x()), round(shape.to().x()));
        int maxX = Math.max(round(shape.from().x()), round(shape.to().x()));
        int minY = Math.min(round(shape.from().y()), round(shape.to().y()));
        int maxY = Math.max(round(shape.from().y()), round(shape.to().y()));
        int minZ = Math.min(round(shape.from().z()), round(shape.to().z()));
        int maxZ = Math.max(round(shape.from().z()), round(shape.to().z()));
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (shape.fill()) {
                        put(out, x, y, z, shape.block());
                    } else {
                        boolean onFace = x == minX || x == maxX
                                || y == minY || y == maxY
                                || z == minZ || z == maxZ;
                        if (onFace) {
                            put(out, x, y, z, shape.block());
                        }
                    }
                }
            }
        }
    }

    private static void rasterizeSphere(SphereShape shape, Map<BlockPos, BlueprintBlock> out) {
        rasterizeBall(shape.origin(), shape.radius(), shape.fill(), false, shape.block(), out);
    }

    private static void rasterizeDome(DomeShape shape, Map<BlockPos, BlueprintBlock> out) {
        rasterizeBall(shape.origin(), shape.radius(), shape.fill(), true, shape.block(), out);
    }

    /**
     * @param domeOnly if true, only voxels with {@code y >= origin.y} (upper hemisphere including equator)
     */
    private static void rasterizeBall(
            BlueprintLocation origin,
            double radius,
            boolean fill,
            boolean domeOnly,
            BlueprintBlock block,
            Map<BlockPos, BlueprintBlock> out
    ) {
        double ox = origin.x();
        double oy = origin.y();
        double oz = origin.z();
        double r2 = radius * radius;
        int minX = floor(ox - radius);
        int maxX = ceil(ox + radius);
        int minY = domeOnly ? round(oy) : floor(oy - radius);
        int maxY = ceil(oy + radius);
        int minZ = floor(oz - radius);
        int maxZ = ceil(oz + radius);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (domeOnly && y < round(oy)) {
                        continue;
                    }
                    double dx = x - ox;
                    double dy = y - oy;
                    double dz = z - oz;
                    double dist2 = dx * dx + dy * dy + dz * dz;
                    if (dist2 > r2) {
                        continue;
                    }
                    if (fill) {
                        put(out, x, y, z, block);
                    } else if (isSurfaceVoxel(ox, oy, oz, r2, x, y, z, domeOnly, round(oy))) {
                        put(out, x, y, z, block);
                    }
                }
            }
        }
    }

    private static boolean isSurfaceVoxel(
            double ox,
            double oy,
            double oz,
            double r2,
            int x,
            int y,
            int z,
            boolean domeOnly,
            int baseY
    ) {
        // Surface if any 6-neighbor is outside the ball (or below the dome base).
        return isOutsideBallOrBelowDome(ox, oy, oz, r2, x + 1, y, z, domeOnly, baseY)
                || isOutsideBallOrBelowDome(ox, oy, oz, r2, x - 1, y, z, domeOnly, baseY)
                || isOutsideBallOrBelowDome(ox, oy, oz, r2, x, y + 1, z, domeOnly, baseY)
                || isOutsideBallOrBelowDome(ox, oy, oz, r2, x, y - 1, z, domeOnly, baseY)
                || isOutsideBallOrBelowDome(ox, oy, oz, r2, x, y, z + 1, domeOnly, baseY)
                || isOutsideBallOrBelowDome(ox, oy, oz, r2, x, y, z - 1, domeOnly, baseY);
    }

    private static boolean isOutsideBallOrBelowDome(
            double ox,
            double oy,
            double oz,
            double r2,
            int x,
            int y,
            int z,
            boolean domeOnly,
            int baseY
    ) {
        if (domeOnly && y < baseY) {
            return true;
        }
        double dx = x - ox;
        double dy = y - oy;
        double dz = z - oz;
        return dx * dx + dy * dy + dz * dz > r2;
    }

    private static void rasterizePolygon(PolygonShape shape, Map<BlockPos, BlueprintBlock> out) {
        int cy = round(shape.origin().y());
        double ox = shape.origin().x();
        double oz = shape.origin().z();
        double r = shape.radius();
        int n = shape.nSides();
        double[] vx = new double[n];
        double[] vz = new double[n];
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2.0 + (2.0 * Math.PI * i) / n;
            vx[i] = ox + r * Math.cos(angle);
            vz[i] = oz + r * Math.sin(angle);
        }
        int minX = floor(ox - r);
        int maxX = ceil(ox + r);
        int minZ = floor(oz - r);
        int maxZ = ceil(oz + r);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (shape.fill()) {
                    if (pointInPolygon(x, z, vx, vz) || onPolygonEdge(x, z, vx, vz)) {
                        put(out, x, cy, z, shape.block());
                    }
                } else if (onPolygonEdge(x, z, vx, vz)) {
                    put(out, x, cy, z, shape.block());
                }
            }
        }
    }

    /** Ray-cast even-odd fill for integer sample points at block centers. */
    private static boolean pointInPolygon(int x, int z, double[] vx, double[] vz) {
        double px = x;
        double pz = z;
        boolean inside = false;
        int n = vx.length;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vx[i];
            double zi = vz[i];
            double xj = vx[j];
            double zj = vz[j];
            boolean intersect = ((zi > pz) != (zj > pz))
                    && (px < (xj - xi) * (pz - zi) / (zj - zi + 0.0) + xi);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    /** True if the block center is within ~0.5 of any polygon edge (Bresenham-ish thickness). */
    private static boolean onPolygonEdge(int x, int z, double[] vx, double[] vz) {
        int n = vx.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            if (distanceToSegment(x, z, vx[i], vz[i], vx[j], vz[j]) <= 0.5) {
                return true;
            }
        }
        return false;
    }

    private static double distanceToSegment(double px, double pz, double ax, double az, double bx, double bz) {
        double dx = bx - ax;
        double dz = bz - az;
        double len2 = dx * dx + dz * dz;
        if (len2 < 1e-12) {
            double ex = px - ax;
            double ez = pz - az;
            return Math.sqrt(ex * ex + ez * ez);
        }
        double t = ((px - ax) * dx + (pz - az) * dz) / len2;
        t = Math.max(0.0, Math.min(1.0, t));
        double qx = ax + t * dx;
        double qz = az + t * dz;
        double ex = px - qx;
        double ez = pz - qz;
        return Math.sqrt(ex * ex + ez * ez);
    }

    private static void put(Map<BlockPos, BlueprintBlock> out, int x, int y, int z, BlueprintBlock block) {
        out.put(new BlockPos(x, y, z), block);
    }

    static int round(double v) {
        return (int) Math.round(v);
    }

    static int floor(double v) {
        return (int) Math.floor(v);
    }

    static int ceil(double v) {
        return (int) Math.ceil(v);
    }
}
