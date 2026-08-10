package com.adamkali.dwm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Hit geometry for First Doctor console controls. Coordinates match
 * {@code FirstDoctorConsoleBlockEntityRenderer} (center, Y×0.8, facing yaw, panel decks).
 *
 * <p>Panel deck chain: panel pivot/yaw → deck bone pivot/pitch → control mount offset.
 */
public final class FirstDoctorConsoleControls {
    /** Panel3 Y rotation in the console model (radians). */
    public static final float PANEL3_YAW_RAD = 2.094395F;

    /** Panel6 Y rotation in the console model (radians). */
    public static final float PANEL6_YAW_RAD = -1.047198F;

    /** Panel pivot Y in model pixels (matches Panel ModelTransform). */
    public static final float PANEL_PIVOT_Y_PX = 14.0F;

    /** Deck bone ModelTransform on each panel (inclined deck). */
    public static final float DECK_PIVOT_Y_PX = 1.714286F;
    public static final float DECK_PIVOT_Z_PX = -13.785714F;
    public static final float DECK_PITCH_RAD = -0.261799F;

    /**
     * Control origin on the deck top in deck-local pixels (center of middle deck cuboid top).
     * Middle cuboid: {@code (-4, 4.081, -0.661) 8×4×6} → top center ≈ {@code (0, 8.081, 2.339)}.
     */
    public static final float CONTROL_MOUNT_X_PX = 0.0F;
    public static final float CONTROL_MOUNT_Y_PX = 8.081F;
    public static final float CONTROL_MOUNT_Z_PX = 2.339F;

    /** Biome selector deck-local X offset (left of Panel3 center). */
    public static final float BIOME_SELECTOR_MOUNT_X_PX = -2.5F;

    /** Planet locator deck-local X offset (right of Panel3 center). */
    public static final float PLANET_LOCATOR_MOUNT_X_PX = 2.5F;

    /** Lever remains centered on its panel. */
    public static final float LEVER_MOUNT_X_PX = CONTROL_MOUNT_X_PX;

    /** Uniform scale — the raw 14px dial is oversized for the Panel3 deck. */
    public static final float SELECTOR_SCALE = 0.1125F;

    /** Uniform scale for the materialisation lever on Panel6. */
    public static final float LEVER_SCALE = 0.2F;

    /** Full selector footprint in selector-local pixels (14×2×14) before {@link #SELECTOR_SCALE}. */
    private static final float SEL_MIN_X = -7.0F;
    private static final float SEL_MIN_Y = 0.0F;
    private static final float SEL_MIN_Z = -7.0F;
    private static final float SEL_MAX_X = 7.0F;
    private static final float SEL_MAX_Y = 2.0F;
    private static final float SEL_MAX_Z = 7.0F;

    /** Lever footprint in lever-local pixels before {@link #LEVER_SCALE}. */
    private static final float LEV_MIN_X = -4.0F;
    private static final float LEV_MIN_Y = 0.0F;
    private static final float LEV_MIN_Z = -9.0F;
    private static final float LEV_MAX_X = 3.0F;
    private static final float LEV_MAX_Y = 8.2F;
    private static final float LEV_MAX_Z = 9.0F;

    private static final float Y_SCALE = 0.8F;
    private static final float PX = 1.0F / 16.0F;
    private static final double REACH = 5.0;

    private FirstDoctorConsoleControls() {
    }

    /**
     * Block-local AABB of the biome selector for the given console facing
     * (relative to the block's min corner).
     */
    public static AABB biomeSelectorBox(Direction facing) {
        return controlBox(facing, PANEL3_YAW_RAD, SELECTOR_SCALE, BIOME_SELECTOR_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
    }

    public static AABB biomeSelectorWorldBox(BlockPos pos, Direction facing) {
        return biomeSelectorBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isBiomeSelectorHit(Direction facing, Vec3 blockLocalHit) {
        return biomeSelectorBox(facing).contains(blockLocalHit);
    }

    public static boolean isBiomeSelectorHit(Direction facing, BlockPos pos, Vec3 worldHit) {
        Vec3 local = worldHit.subtract(pos.getX(), pos.getY(), pos.getZ());
        return isBiomeSelectorHit(facing, local);
    }

    /**
     * True when the player's look ray intersects the biome-selector AABB.
     * Prefer this over block-outline hit positions (those land on the coarse console box).
     */
    public static boolean isBiomeSelectorLookHit(Direction facing, BlockPos pos, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        return isBiomeSelectorLookHit(facing, pos, eye, look, REACH);
    }

    public static boolean isBiomeSelectorLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(biomeSelectorWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    public static AABB planetLocatorBox(Direction facing) {
        return controlBox(facing, PANEL3_YAW_RAD, SELECTOR_SCALE, PLANET_LOCATOR_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
    }

    public static AABB planetLocatorWorldBox(BlockPos pos, Direction facing) {
        return planetLocatorBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isPlanetLocatorLookHit(Direction facing, BlockPos pos, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        return isPlanetLocatorLookHit(facing, pos, eye, look, REACH);
    }

    public static boolean isPlanetLocatorLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(planetLocatorWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    public static AABB materialisationLeverBox(Direction facing) {
        return controlBox(facing, PANEL6_YAW_RAD, LEVER_SCALE, LEVER_MOUNT_X_PX,
                LEV_MIN_X, LEV_MIN_Y, LEV_MIN_Z, LEV_MAX_X, LEV_MAX_Y, LEV_MAX_Z);
    }

    public static AABB materialisationLeverWorldBox(BlockPos pos, Direction facing) {
        return materialisationLeverBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isMaterialisationLeverLookHit(Direction facing, BlockPos pos, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        return isMaterialisationLeverLookHit(facing, pos, eye, look, REACH);
    }

    public static boolean isMaterialisationLeverLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(materialisationLeverWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    /**
     * When biome and planet rays both hit, returns {@code true} if the biome AABB is closer.
     */
    public static boolean preferBiomeOverPlanet(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        double biomeDist = lookHitDistance(biomeSelectorWorldBox(pos, facing), eyePos, lookDir, reach);
        double planetDist = lookHitDistance(planetLocatorWorldBox(pos, facing), eyePos, lookDir, reach);
        if (biomeDist < 0) {
            return false;
        }
        if (planetDist < 0) {
            return true;
        }
        return biomeDist <= planetDist;
    }

    public static boolean preferBiomeOverPlanet(Direction facing, BlockPos pos, Player player) {
        return preferBiomeOverPlanet(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    /**
     * Transforms a point in biome-selector-local model pixels into block-local space (Panel3).
     */
    static Vec3 selectorLocalToBlockLocal(double px, double py, double pz, Direction facing) {
        return controlLocalToBlockLocal(px, py, pz, facing, PANEL3_YAW_RAD, SELECTOR_SCALE, BIOME_SELECTOR_MOUNT_X_PX);
    }

    /**
     * Transforms a point in planet-locator-local model pixels into block-local space (Panel3).
     */
    static Vec3 planetLocatorLocalToBlockLocal(double px, double py, double pz, Direction facing) {
        return controlLocalToBlockLocal(px, py, pz, facing, PANEL3_YAW_RAD, SELECTOR_SCALE, PLANET_LOCATOR_MOUNT_X_PX);
    }

    /**
     * Transforms a point in lever-local model pixels into block-local space (Panel6).
     */
    static Vec3 leverLocalToBlockLocal(double px, double py, double pz, Direction facing) {
        return controlLocalToBlockLocal(px, py, pz, facing, PANEL6_YAW_RAD, LEVER_SCALE, LEVER_MOUNT_X_PX);
    }

    /** Horizontal distance from block center to biome selector center (for tests / tuning). */
    public static double selectorDistanceFromCenter(Direction facing) {
        Vec3 c = biomeSelectorBox(facing).getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
    }

    /** Horizontal distance from block center to planet locator center (for tests / tuning). */
    public static double planetLocatorDistanceFromCenter(Direction facing) {
        Vec3 c = planetLocatorBox(facing).getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
    }

    /** Horizontal distance from block center to lever center (for tests / tuning). */
    public static double leverDistanceFromCenter(Direction facing) {
        Vec3 c = materialisationLeverBox(facing).getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
    }

    private static boolean lookHitsBox(AABB box, Vec3 eyePos, Vec3 lookDir, double reach) {
        return lookHitDistance(box, eyePos, lookDir, reach) >= 0.0;
    }

    /** Distance along the look ray to the hit, or {@code -1} when missing. */
    private static double lookHitDistance(AABB box, Vec3 eyePos, Vec3 lookDir, double reach) {
        Vec3 end = eyePos.add(lookDir.normalize().scale(reach));
        return box.clip(eyePos, end)
                .map(hit -> hit.distanceTo(eyePos))
                .orElse(-1.0);
    }

    private static AABB controlBox(
            Direction facing,
            float panelYawRad,
            float scale,
            float mountXPx,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    ) {
        double outMinX = Double.POSITIVE_INFINITY;
        double outMinY = Double.POSITIVE_INFINITY;
        double outMinZ = Double.POSITIVE_INFINITY;
        double outMaxX = Double.NEGATIVE_INFINITY;
        double outMaxY = Double.NEGATIVE_INFINITY;
        double outMaxZ = Double.NEGATIVE_INFINITY;

        float[] xs = {minX, maxX};
        float[] ys = {minY, maxY};
        float[] zs = {minZ, maxZ};
        for (float x : xs) {
            for (float y : ys) {
                for (float z : zs) {
                    Vec3 p = controlLocalToBlockLocal(x, y, z, facing, panelYawRad, scale, mountXPx);
                    outMinX = Math.min(outMinX, p.x);
                    outMinY = Math.min(outMinY, p.y);
                    outMinZ = Math.min(outMinZ, p.z);
                    outMaxX = Math.max(outMaxX, p.x);
                    outMaxY = Math.max(outMaxY, p.y);
                    outMaxZ = Math.max(outMaxZ, p.z);
                }
            }
        }
        final double pad = 0.08;
        return new AABB(
                outMinX - pad, outMinY - pad, outMinZ - pad,
                outMaxX + pad, outMaxY + pad, outMaxZ + pad
        );
    }

    static Vec3 controlLocalToBlockLocal(
            double px,
            double py,
            double pz,
            Direction facing,
            float panelYawRad,
            float scale,
            float mountXPx
    ) {
        double x = px * scale + mountXPx;
        double y = py * scale + CONTROL_MOUNT_Y_PX;
        double z = pz * scale + CONTROL_MOUNT_Z_PX;

        double cosP = Math.cos(DECK_PITCH_RAD);
        double sinP = Math.sin(DECK_PITCH_RAD);
        double y1 = y * cosP - z * sinP;
        double z1 = y * sinP + z * cosP;
        double x1 = x;
        y1 += DECK_PIVOT_Y_PX;
        z1 += DECK_PIVOT_Z_PX;

        double cos = Math.cos(panelYawRad);
        double sin = Math.sin(panelYawRad);
        double x2 = x1 * cos + z1 * sin;
        double z2 = -x1 * sin + z1 * cos;
        double y2 = y1 + PANEL_PIVOT_Y_PX;

        x2 *= PX;
        y2 *= PX * Y_SCALE;
        z2 *= PX;

        float yawRad = (float) Math.toRadians(-Direction.getYRot(facing));
        double cosF = Math.cos(yawRad);
        double sinF = Math.sin(yawRad);
        double x3 = x2 * cosF + z2 * sinF;
        double z3 = -x2 * sinF + z2 * cosF;

        return new Vec3(x3 + 0.5, y2, z3 + 0.5);
    }
}
