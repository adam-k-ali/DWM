package com.adamkali.dwm.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Hit geometry for First Doctor console controls. Coordinates match
 * {@code FirstDoctorConsoleBlockEntityRenderer} (center, Y×0.8, facing yaw, Panel3 deck).
 *
 * <p>Panel3 deck chain mirrors the model: Panel3 pivot/yaw → bone9 pivot/pitch → deck-top offset.
 */
public final class FirstDoctorConsoleControls {
    /** Panel3 Y rotation in the console model (radians). */
    public static final float PANEL3_YAW_RAD = 2.094395F;

    /** Panel pivot Y in model pixels (matches Panel3 ModelTransform). */
    public static final float PANEL_PIVOT_Y_PX = 14.0F;

    /** bone9 ModelTransform on Panel3 (inclined deck). */
    public static final float DECK_PIVOT_Y_PX = 1.714286F;
    public static final float DECK_PIVOT_Z_PX = -13.785714F;
    public static final float DECK_PITCH_RAD = -0.261799F;

    /**
     * Selector origin on the deck top in bone9-local pixels (center of middle deck cuboid top).
     * Middle cuboid: {@code (-4, 4.081, -0.661) 8×4×6} → top center ≈ {@code (0, 8.081, 2.339)}.
     */
    public static final float SELECTOR_MOUNT_X_PX = 0.0F;
    public static final float SELECTOR_MOUNT_Y_PX = 8.081F;
    public static final float SELECTOR_MOUNT_Z_PX = 2.339F;

    /** Uniform scale — the raw 14px dial is oversized for the Panel3 deck. */
    public static final float SELECTOR_SCALE = 0.1125F;

    /** Full selector footprint in selector-local pixels (14×2×14) before {@link #SELECTOR_SCALE}. */
    private static final float SEL_MIN_X = -7.0F;
    private static final float SEL_MIN_Y = 0.0F;
    private static final float SEL_MIN_Z = -7.0F;
    private static final float SEL_MAX_X = 7.0F;
    private static final float SEL_MAX_Y = 2.0F;
    private static final float SEL_MAX_Z = 7.0F;

    private static final float Y_SCALE = 0.8F;
    private static final float PX = 1.0F / 16.0F;
    private static final double REACH = 5.0;

    private FirstDoctorConsoleControls() {
    }

    /**
     * Block-local AABB of the biome selector for the given console facing
     * (relative to the block's min corner).
     */
    public static Box biomeSelectorBox(Direction facing) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        float[] xs = {SEL_MIN_X, SEL_MAX_X};
        float[] ys = {SEL_MIN_Y, SEL_MAX_Y};
        float[] zs = {SEL_MIN_Z, SEL_MAX_Z};
        for (float x : xs) {
            for (float y : ys) {
                for (float z : zs) {
                    Vec3d p = selectorLocalToBlockLocal(x, y, z, facing);
                    minX = Math.min(minX, p.x);
                    minY = Math.min(minY, p.y);
                    minZ = Math.min(minZ, p.z);
                    maxX = Math.max(maxX, p.x);
                    maxY = Math.max(maxY, p.y);
                    maxZ = Math.max(maxZ, p.z);
                }
            }
        }
        // Generous pad: outline hits and thin tilted slabs need forgiveness.
        final double pad = 0.08;
        return new Box(minX - pad, minY - pad, minZ - pad, maxX + pad, maxY + pad, maxZ + pad);
    }

    public static Box biomeSelectorWorldBox(BlockPos pos, Direction facing) {
        return biomeSelectorBox(facing).offset(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isBiomeSelectorHit(Direction facing, Vec3d blockLocalHit) {
        return biomeSelectorBox(facing).contains(blockLocalHit);
    }

    public static boolean isBiomeSelectorHit(Direction facing, BlockPos pos, Vec3d worldHit) {
        Vec3d local = worldHit.subtract(pos.getX(), pos.getY(), pos.getZ());
        return isBiomeSelectorHit(facing, local);
    }

    /**
     * True when the player's look ray intersects the biome-selector AABB.
     * Prefer this over block-outline hit positions (those land on the coarse console box).
     */
    public static boolean isBiomeSelectorLookHit(Direction facing, BlockPos pos, PlayerEntity player) {
        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        return isBiomeSelectorLookHit(facing, pos, eye, look, REACH);
    }

    public static boolean isBiomeSelectorLookHit(
            Direction facing,
            BlockPos pos,
            Vec3d eyePos,
            Vec3d lookDir,
            double reach
    ) {
        Box box = biomeSelectorWorldBox(pos, facing);
        Vec3d end = eyePos.add(lookDir.normalize().multiply(reach));
        return box.raycast(eyePos, end).isPresent();
    }

    /**
     * Transforms a point in selector-local model pixels into block-local space.
     */
    static Vec3d selectorLocalToBlockLocal(double px, double py, double pz, Direction facing) {
        // Selector scale about origin, then bone9-local mount.
        double x = px * SELECTOR_SCALE + SELECTOR_MOUNT_X_PX;
        double y = py * SELECTOR_SCALE + SELECTOR_MOUNT_Y_PX;
        double z = pz * SELECTOR_SCALE + SELECTOR_MOUNT_Z_PX;

        // bone9 pitch (rotation about X), then deck pivot translate.
        // Matches MatrixStack / ModelPart: y' = y cos − z sin, z' = y sin + z cos.
        double cosP = Math.cos(DECK_PITCH_RAD);
        double sinP = Math.sin(DECK_PITCH_RAD);
        double y1 = y * cosP - z * sinP;
        double z1 = y * sinP + z * cosP;
        double x1 = x;
        y1 += DECK_PIVOT_Y_PX;
        z1 += DECK_PIVOT_Z_PX;

        // Panel3 yaw — must match RotationAxis.POSITIVE_Y / ModelPart (Minecraft/JOML):
        // x' = x cos + z sin, z' = −x sin + z cos.
        double cos = Math.cos(PANEL3_YAW_RAD);
        double sin = Math.sin(PANEL3_YAW_RAD);
        double x2 = x1 * cos + z1 * sin;
        double z2 = -x1 * sin + z1 * cos;
        double y2 = y1 + PANEL_PIVOT_Y_PX;

        x2 *= PX;
        y2 *= PX * Y_SCALE;
        z2 *= PX;

        float yawRad = (float) Math.toRadians(-Direction.getHorizontalDegreesOrThrow(facing));
        double cosF = Math.cos(yawRad);
        double sinF = Math.sin(yawRad);
        double x3 = x2 * cosF + z2 * sinF;
        double z3 = -x2 * sinF + z2 * cosF;

        return new Vec3d(x3 + 0.5, y2, z3 + 0.5);
    }

    /** Horizontal distance from block center to selector center (for tests / tuning). */
    public static double selectorDistanceFromCenter(Direction facing) {
        Vec3d c = biomeSelectorBox(facing).getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
    }
}
