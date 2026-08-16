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
    /**
     * Hex faces of the First Doctor console. Opposite pairs: Environment↔Security,
     * Communications↔Systems, Navigation↔Helm.
     */
    public enum ConsolePanel {
        ENVIRONMENT(1, 0.0F, "Environment"),
        COMMUNICATIONS(2, 1.047198F, "Communications"),
        NAVIGATION(3, 2.094395F, "Navigation"),
        SECURITY(4, -3.141593F, "Security"),
        SYSTEMS(5, -2.094395F, "Systems"),
        HELM(6, -1.047198F, "Helm");

        private final int index;
        private final float yawRad;
        private final String purpose;

        ConsolePanel(int index, float yawRad, String purpose) {
            this.index = index;
            this.yawRad = yawRad;
            this.purpose = purpose;
        }

        public int index() {
            return index;
        }

        public float yawRad() {
            return yawRad;
        }

        public String purpose() {
            return purpose;
        }
    }

    /** Panel1 Y rotation in the console model (radians). */
    public static final float PANEL1_YAW_RAD = ConsolePanel.ENVIRONMENT.yawRad();

    /** Panel2 Y rotation in the console model (radians). */
    public static final float PANEL2_YAW_RAD = ConsolePanel.COMMUNICATIONS.yawRad();

    /** Panel3 Y rotation in the console model (radians). */
    public static final float PANEL3_YAW_RAD = ConsolePanel.NAVIGATION.yawRad();

    /** Panel4 Y rotation in the console model (radians). */
    public static final float PANEL4_YAW_RAD = ConsolePanel.SECURITY.yawRad();

    /** Panel5 Y rotation in the console model (radians). */
    public static final float PANEL5_YAW_RAD = ConsolePanel.SYSTEMS.yawRad();

    /** Panel6 Y rotation in the console model (radians). */
    public static final float PANEL6_YAW_RAD = ConsolePanel.HELM.yawRad();

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
     * Top (inner) row — toward the rotor, compact widget cuboid top center.
     * Inner cuboid: {@code (-5, 5.081, 5.339) 10×4×5} → top center ≈ {@code (0, 9.081, 7.839)}.
     */
    public static final float TOP_MOUNT_X_PX = 0.0F;
    public static final float TOP_MOUNT_Y_PX = 9.081F;
    public static final float TOP_MOUNT_Z_PX = 7.839F;

    /**
     * Bottom (outer) row — player-facing cuboid top center.
     * Middle-row mounts keep {@link #CONTROL_MOUNT_Y_PX} / {@link #CONTROL_MOUNT_Z_PX}.
     */
    public static final float STABILISERS_MOUNT_X_PX = 0.0F;
    public static final float STABILISERS_MOUNT_Y_PX = 7.081F;
    public static final float STABILISERS_MOUNT_Z_PX = -3.661F;
    public static final float BOTTOM_MOUNT_X_PX = STABILISERS_MOUNT_X_PX;
    public static final float BOTTOM_MOUNT_Y_PX = STABILISERS_MOUNT_Y_PX;
    public static final float BOTTOM_MOUNT_Z_PX = STABILISERS_MOUNT_Z_PX;

    /** Uniform scale — the raw 14px dial is oversized for the Panel3 deck. */
    public static final float SELECTOR_SCALE = 0.1125F;

    /** Uniform scale for the materialisation lever on Panel6. */
    public static final float LEVER_SCALE = 0.2F;

    /** Uniform scale for the fast-return switch on Panel6 (matches lever). */
    public static final float FAST_RETURN_SCALE = LEVER_SCALE;

    /** Uniform scale for the stabilisers control on Panel6 bottom row. */
    public static final float STABILISERS_SCALE = 0.18F;

    /** Shared 16×3×16 environment / refueler dial. */
    public static final float READER_SCALE = SELECTOR_SCALE;

    /** Taller unique radiation mesh on Panel1 bottom. */
    public static final float RADIATION_SCALE = 0.10F;

    /** 18×2×8 telepathic strip. */
    public static final float TELEPATHIC_SCALE = 0.14F;

    /** Cloak lever matches the materialisation lever scale. */
    public static final float CLOAK_SCALE = LEVER_SCALE;

    /** Wide flat door-lock panel. */
    public static final float DOOR_LOCK_SCALE = 0.14F;

    /** Wide 39px coordinate-lock instrument. */
    public static final float COORDINATE_LOCK_SCALE = 0.11F;

    /** Panel1 middle-row reader layout (deck-local X). */
    public static final float OXYGEN_READER_MOUNT_X_PX = -3.75F;
    public static final float PRESSURE_READER_MOUNT_X_PX = 0.0F;
    public static final float TEMPERATURE_READER_MOUNT_X_PX = 3.75F;

    /** Full selector footprint in selector-local pixels (14×2×14) before {@link #SELECTOR_SCALE}. */
    private static final float SEL_MIN_X = -7.0F;
    private static final float SEL_MIN_Y = 0.0F;
    private static final float SEL_MIN_Z = -7.0F;
    private static final float SEL_MAX_X = 7.0F;
    private static final float SEL_MAX_Y = 2.0F;
    private static final float SEL_MAX_Z = 7.0F;

    /**
     * Lever footprint in lever-local pixels before {@link #LEVER_SCALE}.
     * Tight widget bounds (handle), not the decorative 18px base plate.
     */
    private static final float LEV_MIN_X = -2.0F;
    private static final float LEV_MIN_Y = 0.0F;
    private static final float LEV_MIN_Z = -3.0F;
    private static final float LEV_MAX_X = 3.0F;
    private static final float LEV_MAX_Y = 8.0F;
    private static final float LEV_MAX_Z = 3.0F;

    /** Fast-return footprint in switch-local pixels before {@link #FAST_RETURN_SCALE}. */
    private static final float FR_MIN_X = -2.5F;
    private static final float FR_MIN_Y = 0.0F;
    private static final float FR_MIN_Z = -3.0F;
    private static final float FR_MAX_X = 2.5F;
    private static final float FR_MAX_Y = 3.6F;
    private static final float FR_MAX_Z = 3.0F;

    /**
     * Stabilisers footprint in control-local pixels before {@link #STABILISERS_SCALE}.
     * Tight widget bounds, not the decorative 18px base plate.
     */
    private static final float STAB_MIN_X = -4.0F;
    private static final float STAB_MIN_Y = 0.0F;
    private static final float STAB_MIN_Z = -4.0F;
    private static final float STAB_MAX_X = 4.0F;
    private static final float STAB_MAX_Y = 7.0F;
    private static final float STAB_MAX_Z = 4.0F;

    /** Shared reader dial footprint (16×3×16) before {@link #READER_SCALE}. */
    private static final float RDR_MIN_X = -8.0F;
    private static final float RDR_MIN_Y = 0.0F;
    private static final float RDR_MIN_Z = -8.0F;
    private static final float RDR_MAX_X = 8.0F;
    private static final float RDR_MAX_Y = 3.0F;
    private static final float RDR_MAX_Z = 8.0F;

    /** Radiation reader footprint before {@link #RADIATION_SCALE}. */
    private static final float RAD_MIN_X = -7.0F;
    private static final float RAD_MIN_Y = 0.0F;
    private static final float RAD_MIN_Z = -11.0F;
    private static final float RAD_MAX_X = 7.0F;
    private static final float RAD_MAX_Y = 6.0F;
    private static final float RAD_MAX_Z = 8.0F;

    /** Telepathic strip footprint (18×2×8) before {@link #TELEPATHIC_SCALE}. */
    private static final float TEL_MIN_X = -9.0F;
    private static final float TEL_MIN_Y = 0.0F;
    private static final float TEL_MIN_Z = -4.0F;
    private static final float TEL_MAX_X = 9.0F;
    private static final float TEL_MAX_Y = 2.0F;
    private static final float TEL_MAX_Z = 4.0F;

    /** Cloak lever footprint before {@link #CLOAK_SCALE}. */
    private static final float CLK_MIN_X = -2.0F;
    private static final float CLK_MIN_Y = 0.0F;
    private static final float CLK_MIN_Z = -3.0F;
    private static final float CLK_MAX_X = 3.0F;
    private static final float CLK_MAX_Y = 8.0F;
    private static final float CLK_MAX_Z = 3.0F;

    /** Door lock footprint before {@link #DOOR_LOCK_SCALE}. */
    private static final float DLK_MIN_X = -11.0F;
    private static final float DLK_MIN_Y = 0.0F;
    private static final float DLK_MIN_Z = -6.0F;
    private static final float DLK_MAX_X = 11.0F;
    private static final float DLK_MAX_Y = 2.0F;
    private static final float DLK_MAX_Z = 8.0F;

    /** Per-axis coordinate-lock pads on the wide instrument. */
    private static final float CLKX_MIN_X = 7.0F;
    private static final float CLKX_MAX_X = 14.0F;
    private static final float CLKY_MIN_X = 0.0F;
    private static final float CLKY_MAX_X = 6.0F;
    private static final float CLKZ_MIN_X = -8.0F;
    private static final float CLKZ_MAX_X = -1.0F;
    private static final float CLKA_MIN_Y = 0.0F;
    private static final float CLKA_MAX_Y = 3.0F;
    private static final float CLKA_MIN_Z = -12.0F;
    private static final float CLKA_MAX_Z = -6.0F;

    private record ControlLayout(
            float panelYaw,
            float scale,
            float mountX,
            float mountY,
            float mountZ,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    ) {
    }

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
        STABILISERS,
        OXYGEN_READER,
        PRESSURE_READER,
        TEMPERATURE_READER,
        RADIATION_READER,
        REFUELER,
        TELEPATHIC_CIRCUIT,
        CLOAK,
        DOOR_LOCK,
        COORDINATE_LOCK_X,
        COORDINATE_LOCK_Y,
        COORDINATE_LOCK_Z;

        /** Controls that spawn interaction entities (excludes {@link #NONE}). */
        public static LookTarget[] interactiveValues() {
            LookTarget[] all = values();
            LookTarget[] interactive = new LookTarget[all.length - 1];
            System.arraycopy(all, 1, interactive, 0, interactive.length);
            return interactive;
        }
    }

    /**
     * Axis-aligned interaction entity pose for a console control.
     * {@code position} is the bottom-center of the entity in world space.
     */
    public record InteractionPose(Vec3 position, float width, float height) {
        public AABB aabb() {
            double half = width * 0.5;
            return new AABB(
                    position.x - half,
                    position.y,
                    position.z - half,
                    position.x + half,
                    position.y + height,
                    position.z + half
            );
        }
    }

    private FirstDoctorConsoleControls() {
    }

    /**
     * World-space interaction pose for {@code target} on a console at {@code pos}.
     * Returns {@code null} for {@link LookTarget#NONE}.
     *
     * <p>The entity is centered on the control AABB so minimum size clamps expand
     * around the widget instead of only upward (which forced aiming above flat dials).
     */
    public static @Nullable InteractionPose interactionPose(LookTarget target, BlockPos pos, Direction facing) {
        if (target == LookTarget.NONE) {
            return null;
        }
        AABB local = unpaddedBoxFor(target, facing);
        double xSize = local.getXsize();
        double zSize = local.getZsize();
        // Flat dial meshes are only a few scaled pixels tall; clamp so entity picking stays usable.
        float width = (float) Math.max(Math.min(xSize, zSize), 0.18);
        float height = (float) Math.max(local.getYsize(), 0.18);
        Vec3 center = local.getCenter();
        Vec3 bottomCenter = new Vec3(
                pos.getX() + center.x,
                pos.getY() + center.y - height * 0.5,
                pos.getZ() + center.z
        );
        return new InteractionPose(bottomCenter, width, height);
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
        return unpaddedBoxFor(target, facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    private static AABB unpaddedBoxFor(LookTarget target, Direction facing) {
        if (target == LookTarget.NONE) {
            return new AABB(0, 0, 0, 0, 0, 0);
        }
        ControlLayout layout = layout(target);
        return controlBox(
                facing,
                layout.panelYaw,
                layout.scale,
                layout.mountX,
                layout.mountY,
                layout.mountZ,
                layout.minX,
                layout.minY,
                layout.minZ,
                layout.maxX,
                layout.maxY,
                layout.maxZ
        );
    }

    private static ControlLayout layout(LookTarget target) {
        return switch (target) {
            case BIOME_SELECTOR -> middle(PANEL3_YAW_RAD, SELECTOR_SCALE, BIOME_SELECTOR_MOUNT_X_PX,
                    SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
            case WAYPOINT_SELECTOR -> middle(PANEL3_YAW_RAD, SELECTOR_SCALE, WAYPOINT_SELECTOR_MOUNT_X_PX,
                    SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
            case PLAYER_LOCATOR -> middle(PANEL3_YAW_RAD, SELECTOR_SCALE, PLAYER_LOCATOR_MOUNT_X_PX,
                    SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
            case PLANET_LOCATOR -> middle(PANEL3_YAW_RAD, SELECTOR_SCALE, PLANET_LOCATOR_MOUNT_X_PX,
                    SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
            case CHAMELEON_CIRCUIT -> middle(PANEL6_YAW_RAD, SELECTOR_SCALE, CHAMELEON_CIRCUIT_MOUNT_X_PX,
                    SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z);
            case MATERIALISATION_LEVER -> middle(PANEL6_YAW_RAD, LEVER_SCALE, LEVER_MOUNT_X_PX,
                    LEV_MIN_X, LEV_MIN_Y, LEV_MIN_Z, LEV_MAX_X, LEV_MAX_Y, LEV_MAX_Z);
            case FAST_RETURN -> middle(PANEL6_YAW_RAD, FAST_RETURN_SCALE, FAST_RETURN_MOUNT_X_PX,
                    FR_MIN_X, FR_MIN_Y, FR_MIN_Z, FR_MAX_X, FR_MAX_Y, FR_MAX_Z);
            case STABILISERS -> bottom(PANEL6_YAW_RAD, STABILISERS_SCALE, STABILISERS_MOUNT_X_PX,
                    STAB_MIN_X, STAB_MIN_Y, STAB_MIN_Z, STAB_MAX_X, STAB_MAX_Y, STAB_MAX_Z);
            case OXYGEN_READER -> middle(PANEL1_YAW_RAD, READER_SCALE, OXYGEN_READER_MOUNT_X_PX,
                    RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z);
            case PRESSURE_READER -> middle(PANEL1_YAW_RAD, READER_SCALE, PRESSURE_READER_MOUNT_X_PX,
                    RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z);
            case TEMPERATURE_READER -> middle(PANEL1_YAW_RAD, READER_SCALE, TEMPERATURE_READER_MOUNT_X_PX,
                    RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z);
            case RADIATION_READER -> bottom(PANEL1_YAW_RAD, RADIATION_SCALE, 0.0F,
                    RAD_MIN_X, RAD_MIN_Y, RAD_MIN_Z, RAD_MAX_X, RAD_MAX_Y, RAD_MAX_Z);
            case REFUELER -> middle(PANEL5_YAW_RAD, READER_SCALE, 0.0F,
                    RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z);
            case TELEPATHIC_CIRCUIT -> middle(PANEL2_YAW_RAD, TELEPATHIC_SCALE, 0.0F,
                    TEL_MIN_X, TEL_MIN_Y, TEL_MIN_Z, TEL_MAX_X, TEL_MAX_Y, TEL_MAX_Z);
            case CLOAK -> middle(PANEL4_YAW_RAD, CLOAK_SCALE, 0.0F,
                    CLK_MIN_X, CLK_MIN_Y, CLK_MIN_Z, CLK_MAX_X, CLK_MAX_Y, CLK_MAX_Z);
            case DOOR_LOCK -> bottom(PANEL4_YAW_RAD, DOOR_LOCK_SCALE, 0.0F,
                    DLK_MIN_X, DLK_MIN_Y, DLK_MIN_Z, DLK_MAX_X, DLK_MAX_Y, DLK_MAX_Z);
            case COORDINATE_LOCK_X -> bottom(PANEL3_YAW_RAD, COORDINATE_LOCK_SCALE, 0.0F,
                    CLKX_MIN_X, CLKA_MIN_Y, CLKA_MIN_Z, CLKX_MAX_X, CLKA_MAX_Y, CLKA_MAX_Z);
            case COORDINATE_LOCK_Y -> bottom(PANEL3_YAW_RAD, COORDINATE_LOCK_SCALE, 0.0F,
                    CLKY_MIN_X, CLKA_MIN_Y, CLKA_MIN_Z, CLKY_MAX_X, CLKA_MAX_Y, CLKA_MAX_Z);
            case COORDINATE_LOCK_Z -> bottom(PANEL3_YAW_RAD, COORDINATE_LOCK_SCALE, 0.0F,
                    CLKZ_MIN_X, CLKA_MIN_Y, CLKA_MIN_Z, CLKZ_MAX_X, CLKA_MAX_Y, CLKA_MAX_Z);
            case NONE -> new ControlLayout(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        };
    }

    private static ControlLayout middle(
            float yaw,
            float scale,
            float mountX,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    ) {
        return new ControlLayout(
                yaw, scale, mountX, CONTROL_MOUNT_Y_PX, CONTROL_MOUNT_Z_PX,
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static ControlLayout bottom(
            float yaw,
            float scale,
            float mountX,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    ) {
        return new ControlLayout(
                yaw, scale, mountX, BOTTOM_MOUNT_Y_PX, BOTTOM_MOUNT_Z_PX,
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static AABB boxFor(LookTarget target, Direction facing) {
        return unpaddedBoxFor(target, facing);
    }

    public static AABB worldBoxForTarget(LookTarget target, BlockPos pos, Direction facing) {
        return worldBoxFor(target, pos, facing);
    }

    public static double distanceFromCenter(LookTarget target, Direction facing) {
        AABB box = unpaddedBoxFor(target, facing);
        Vec3 c = box.getCenter();
        return Math.hypot(c.x - 0.5, c.z - 0.5);
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
        final double pad = 0.0;
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
