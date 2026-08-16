package com.adamkali.dwm.block;

import org.jetbrains.annotations.Nullable;

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

    /** Panel3 four-dial layout (deck-local X). */
    public static final float BIOME_SELECTOR_MOUNT_X_PX = -3.75F;
    public static final float WAYPOINT_SELECTOR_MOUNT_X_PX = -1.25F;
    public static final float PLAYER_LOCATOR_MOUNT_X_PX = 1.25F;
    public static final float PLANET_LOCATOR_MOUNT_X_PX = 3.75F;

    /** Panel6: chameleon left of center; lever centered; fast return right of lever. */
    public static final float CHAMELEON_CIRCUIT_MOUNT_X_PX = -4.0F;
    public static final float LEVER_MOUNT_X_PX = CONTROL_MOUNT_X_PX;
    public static final float FAST_RETURN_MOUNT_X_PX = 4.0F;

    /**
     * Panel6 bottom (outer) row — player-facing cuboid top center.
     * Middle-row mounts keep {@link #CONTROL_MOUNT_Y_PX} / {@link #CONTROL_MOUNT_Z_PX}.
     */
    public static final float STABILISERS_MOUNT_X_PX = 0.0F;
    public static final float STABILISERS_MOUNT_Y_PX = 7.081F;
    public static final float STABILISERS_MOUNT_Z_PX = -3.661F;

    /** Uniform scale — the raw 14px dial is oversized for the Panel3 deck. */
    public static final float SELECTOR_SCALE = 0.1125F;

    /** Uniform scale for the materialisation lever on Panel6. */
    public static final float LEVER_SCALE = 0.2F;

    /** Uniform scale for the fast-return switch on Panel6 (matches lever). */
    public static final float FAST_RETURN_SCALE = LEVER_SCALE;

    /** Uniform scale for the stabilisers control on Panel6 bottom row. */
    public static final float STABILISERS_SCALE = 0.18F;

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

    /** Fast-return footprint in switch-local pixels before {@link #FAST_RETURN_SCALE}. */
    private static final float FR_MIN_X = -3.0F;
    private static final float FR_MIN_Y = 0.0F;
    private static final float FR_MIN_Z = -8.0F;
    private static final float FR_MAX_X = 3.0F;
    private static final float FR_MAX_Y = 3.6F;
    private static final float FR_MAX_Z = 8.3F;

    /** Stabilisers footprint in control-local pixels before {@link #STABILISERS_SCALE}. */
    private static final float STAB_MIN_X = -9.0F;
    private static final float STAB_MIN_Y = 0.0F;
    private static final float STAB_MIN_Z = -9.0F;
    private static final float STAB_MAX_X = 8.0F;
    private static final float STAB_MAX_Y = 8.2F;
    private static final float STAB_MAX_Z = 9.0F;

    private static final float Y_SCALE = 0.8F;
    private static final float PX = 1.0F / 16.0F;
    private static final double REACH = 5.0;

    /** Panel3 dials resolved by look-ray (prefer closest on overlap). */
    public enum Panel3Control {
        BIOME,
        WAYPOINT,
        PLAYER,
        PLANET
    }

    /** Panel6 controls resolved by look-ray (prefer closest on overlap). */
    public enum Panel6Control {
        CHAMELEON,
        LEVER,
        FAST_RETURN,
        STABILISERS
    }

    /**
     * Unified look-ray target for console interaction (GUI / HUD). Prefer closest on overlap.
     */
    public enum LookTarget {
        NONE,
        BIOME_SELECTOR,
        WAYPOINT_SELECTOR,
        PLAYER_LOCATOR,
        PLANET_LOCATOR,
        CHAMELEON_CIRCUIT,
        MATERIALISATION_LEVER,
        FAST_RETURN,
        STABILISERS
    }

    private FirstDoctorConsoleControls() {
    }

    /**
     * Block-local AABB of the biome selector for the given console facing
     * (relative to the block's min corner).
     */
    public static AABB biomeSelectorBox(Direction facing) {
        return selectorBox(facing, BIOME_SELECTOR_MOUNT_X_PX);
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

    public static AABB waypointSelectorBox(Direction facing) {
        return selectorBox(facing, WAYPOINT_SELECTOR_MOUNT_X_PX);
    }

    public static AABB waypointSelectorWorldBox(BlockPos pos, Direction facing) {
        return waypointSelectorBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isWaypointSelectorLookHit(Direction facing, BlockPos pos, Player player) {
        return isWaypointSelectorLookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    public static boolean isWaypointSelectorLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(waypointSelectorWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    public static AABB playerLocatorBox(Direction facing) {
        return selectorBox(facing, PLAYER_LOCATOR_MOUNT_X_PX);
    }

    public static AABB playerLocatorWorldBox(BlockPos pos, Direction facing) {
        return playerLocatorBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isPlayerLocatorLookHit(Direction facing, BlockPos pos, Player player) {
        return isPlayerLocatorLookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    public static boolean isPlayerLocatorLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(playerLocatorWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    public static AABB planetLocatorBox(Direction facing) {
        return selectorBox(facing, PLANET_LOCATOR_MOUNT_X_PX);
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

    public static AABB chameleonCircuitBox(Direction facing) {
        return controlBox(facing, PANEL6_YAW_RAD, SELECTOR_SCALE, CHAMELEON_CIRCUIT_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
    }

    public static AABB chameleonCircuitWorldBox(BlockPos pos, Direction facing) {
        return chameleonCircuitBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isChameleonCircuitLookHit(Direction facing, BlockPos pos, Player player) {
        return isChameleonCircuitLookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    public static boolean isChameleonCircuitLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(chameleonCircuitWorldBox(pos, facing), eyePos, lookDir, reach);
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

    public static AABB fastReturnBox(Direction facing) {
        return controlBox(facing, PANEL6_YAW_RAD, FAST_RETURN_SCALE, FAST_RETURN_MOUNT_X_PX,
                FR_MIN_X, FR_MIN_Y, FR_MIN_Z, FR_MAX_X, FR_MAX_Y, FR_MAX_Z);
    }

    public static AABB fastReturnWorldBox(BlockPos pos, Direction facing) {
        return fastReturnBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isFastReturnLookHit(Direction facing, BlockPos pos, Player player) {
        return isFastReturnLookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    public static boolean isFastReturnLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(fastReturnWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    public static AABB stabilisersBox(Direction facing) {
        return controlBox(
                facing,
                PANEL6_YAW_RAD,
                STABILISERS_SCALE,
                STABILISERS_MOUNT_X_PX,
                STABILISERS_MOUNT_Y_PX,
                STABILISERS_MOUNT_Z_PX,
                STAB_MIN_X,
                STAB_MIN_Y,
                STAB_MIN_Z,
                STAB_MAX_X,
                STAB_MAX_Y,
                STAB_MAX_Z
        );
    }

    public static AABB stabilisersWorldBox(BlockPos pos, Direction facing) {
        return stabilisersBox(facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isStabilisersLookHit(Direction facing, BlockPos pos, Player player) {
        return isStabilisersLookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    public static boolean isStabilisersLookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        return lookHitsBox(stabilisersWorldBox(pos, facing), eyePos, lookDir, reach);
    }

    /**
     * When multiple Panel3 dials are along the look ray, returns the closest hit.
     * Ties (coplanar dial tops) break toward the AABB whose center is nearest the eye in XZ.
     * Returns {@code null} when none are hit.
     */
    public static @Nullable Panel3Control resolvePanel3LookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        Panel3Control best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        double bestHoriz = Double.POSITIVE_INFINITY;
        for (Panel3Control control : Panel3Control.values()) {
            AABB box = panel3WorldBox(control, pos, facing);
            double dist = lookHitDistance(box, eyePos, lookDir, reach);
            if (dist < 0.0) {
                continue;
            }
            Vec3 center = box.getCenter();
            double horiz = horizontalDistanceSq(eyePos, center);
            if (dist < bestDist || (dist == bestDist && horiz < bestHoriz)) {
                bestDist = dist;
                bestHoriz = horiz;
                best = control;
            }
        }
        return best;
    }

    public static @Nullable Panel3Control resolvePanel3LookHit(Direction facing, BlockPos pos, Player player) {
        return resolvePanel3LookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    /**
     * When Panel6 control rays overlap, returns the closer control.
     * Returns {@code null} when none are hit.
     */
    public static @Nullable Panel6Control resolvePanel6LookHit(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        Panel6Control best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        double bestHoriz = Double.POSITIVE_INFINITY;
        for (Panel6Control control : Panel6Control.values()) {
            AABB box = panel6WorldBox(control, pos, facing);
            double dist = lookHitDistance(box, eyePos, lookDir, reach);
            if (dist < 0.0) {
                continue;
            }
            double horiz = horizontalDistanceSq(eyePos, box.getCenter());
            if (dist < bestDist || (dist == bestDist && horiz < bestHoriz)) {
                bestDist = dist;
                bestHoriz = horiz;
                best = control;
            }
        }
        return best;
    }

    public static @Nullable Panel6Control resolvePanel6LookHit(Direction facing, BlockPos pos, Player player) {
        return resolvePanel6LookHit(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    /**
     * Resolves the closest console control along the player's look ray across Panel3 and Panel6.
     * Returns {@link LookTarget#NONE} when nothing is hit.
     */
    public static LookTarget resolveLookTarget(Direction facing, BlockPos pos, Player player) {
        return resolveLookTarget(facing, pos, player.getEyePosition(), player.getViewVector(1.0F), REACH);
    }

    public static LookTarget resolveLookTarget(
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        LookTarget best = LookTarget.NONE;
        double bestDist = Double.POSITIVE_INFINITY;
        double bestHoriz = Double.POSITIVE_INFINITY;
        for (LookTarget candidate : LookTarget.values()) {
            if (candidate == LookTarget.NONE) {
                continue;
            }
            AABB box = worldBoxFor(candidate, pos, facing);
            double dist = lookHitDistance(box, eyePos, lookDir, reach);
            if (dist < 0.0) {
                continue;
            }
            double horiz = horizontalDistanceSq(eyePos, box.getCenter());
            if (dist < bestDist || (dist == bestDist && horiz < bestHoriz)) {
                bestDist = dist;
                bestHoriz = horiz;
                best = candidate;
            }
        }
        return best;
    }

    private static AABB worldBoxFor(LookTarget target, BlockPos pos, Direction facing) {
        return switch (target) {
            case BIOME_SELECTOR -> biomeSelectorWorldBox(pos, facing);
            case WAYPOINT_SELECTOR -> waypointSelectorWorldBox(pos, facing);
            case PLAYER_LOCATOR -> playerLocatorWorldBox(pos, facing);
            case PLANET_LOCATOR -> planetLocatorWorldBox(pos, facing);
            case CHAMELEON_CIRCUIT -> chameleonCircuitWorldBox(pos, facing);
            case MATERIALISATION_LEVER -> materialisationLeverWorldBox(pos, facing);
            case FAST_RETURN -> fastReturnWorldBox(pos, facing);
            case STABILISERS -> stabilisersWorldBox(pos, facing);
            case NONE -> new AABB(0, 0, 0, 0, 0, 0);
        };
    }

    /**
     * When biome and planet rays both hit, returns {@code true} if the biome AABB is closer.
     * Prefer {@link #resolveLookTarget} for new callers.
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

    /** Horizontal distance from block center to fast-return switch center (for tests / tuning). */
    public static double fastReturnDistanceFromCenter(Direction facing) {
        Vec3 c = fastReturnBox(facing).getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
    }

    /** Horizontal distance from block center to stabilisers control center (for tests / tuning). */
    public static double stabilisersDistanceFromCenter(Direction facing) {
        Vec3 c = stabilisersBox(facing).getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
    }

    private static AABB selectorBox(Direction facing, float mountXPx) {
        return controlBox(facing, PANEL3_YAW_RAD, SELECTOR_SCALE, mountXPx,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
    }

    private static AABB panel3WorldBox(Panel3Control control, BlockPos pos, Direction facing) {
        return switch (control) {
            case BIOME -> biomeSelectorWorldBox(pos, facing);
            case WAYPOINT -> waypointSelectorWorldBox(pos, facing);
            case PLAYER -> playerLocatorWorldBox(pos, facing);
            case PLANET -> planetLocatorWorldBox(pos, facing);
        };
    }

    private static AABB panel6WorldBox(Panel6Control control, BlockPos pos, Direction facing) {
        return switch (control) {
            case CHAMELEON -> chameleonCircuitWorldBox(pos, facing);
            case LEVER -> materialisationLeverWorldBox(pos, facing);
            case FAST_RETURN -> fastReturnWorldBox(pos, facing);
            case STABILISERS -> stabilisersWorldBox(pos, facing);
        };
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

    private static double horizontalDistanceSq(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
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
        return controlBox(
                facing,
                panelYawRad,
                scale,
                mountXPx,
                CONTROL_MOUNT_Y_PX,
                CONTROL_MOUNT_Z_PX,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

    private static AABB controlBox(
            Direction facing,
            float panelYawRad,
            float scale,
            float mountXPx,
            float mountYPx,
            float mountZPx,
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
                    Vec3 p = controlLocalToBlockLocal(
                            x, y, z, facing, panelYawRad, scale, mountXPx, mountYPx, mountZPx);
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
        return controlLocalToBlockLocal(
                px, py, pz, facing, panelYawRad, scale, mountXPx, CONTROL_MOUNT_Y_PX, CONTROL_MOUNT_Z_PX);
    }

    static Vec3 controlLocalToBlockLocal(
            double px,
            double py,
            double pz,
            Direction facing,
            float panelYawRad,
            float scale,
            float mountXPx,
            float mountYPx,
            float mountZPx
    ) {
        double x = px * scale + mountXPx;
        double y = py * scale + mountYPx;
        double z = pz * scale + mountZPx;

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
