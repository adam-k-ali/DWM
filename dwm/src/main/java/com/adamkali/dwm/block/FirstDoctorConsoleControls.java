package com.adamkali.dwm.block;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

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
 * Per-control layout lives in the {@link ConsoleControlSpec} catalog.
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
    public static final float BOTTOM_MOUNT_X_PX = 0.0F;
    public static final float BOTTOM_MOUNT_Y_PX = 7.081F;
    public static final float BOTTOM_MOUNT_Z_PX = -3.661F;

    private static final float BIOME_SELECTOR_MOUNT_X_PX = -3.75F;
    private static final float WAYPOINT_SELECTOR_MOUNT_X_PX = -1.25F;
    private static final float PLAYER_LOCATOR_MOUNT_X_PX = 1.25F;
    private static final float PLANET_LOCATOR_MOUNT_X_PX = 3.75F;
    private static final float CHAMELEON_CIRCUIT_MOUNT_X_PX = -4.0F;
    private static final float LEVER_MOUNT_X_PX = CONTROL_MOUNT_X_PX;
    private static final float FAST_RETURN_MOUNT_X_PX = 4.0F;
    private static final float STABILISERS_MOUNT_X_PX = 0.0F;
    private static final float OXYGEN_READER_MOUNT_X_PX = -3.75F;
    private static final float PRESSURE_READER_MOUNT_X_PX = 0.0F;
    private static final float TEMPERATURE_READER_MOUNT_X_PX = 3.75F;

    private static final float SELECTOR_SCALE = 0.1125F;
    private static final float LEVER_SCALE = 0.2F;
    private static final float FAST_RETURN_SCALE = LEVER_SCALE;
    private static final float STABILISERS_SCALE = 0.18F;
    private static final float READER_SCALE = SELECTOR_SCALE;
    private static final float RADIATION_SCALE = 0.10F;
    private static final float TELEPATHIC_SCALE = 0.14F;
    private static final float CLOAK_SCALE = LEVER_SCALE;
    private static final float DOOR_LOCK_SCALE = 0.14F;
    private static final float COORDINATE_LOCK_SCALE = 0.11F;

    private static final float SEL_MIN_X = -7.0F;
    private static final float SEL_MIN_Y = 0.0F;
    private static final float SEL_MIN_Z = -7.0F;
    private static final float SEL_MAX_X = 7.0F;
    private static final float SEL_MAX_Y = 2.0F;
    private static final float SEL_MAX_Z = 7.0F;

    private static final float LEV_MIN_X = -2.0F;
    private static final float LEV_MIN_Y = 0.0F;
    private static final float LEV_MIN_Z = -3.0F;
    private static final float LEV_MAX_X = 3.0F;
    private static final float LEV_MAX_Y = 8.0F;
    private static final float LEV_MAX_Z = 3.0F;

    private static final float FR_MIN_X = -2.5F;
    private static final float FR_MIN_Y = 0.0F;
    private static final float FR_MIN_Z = -3.0F;
    private static final float FR_MAX_X = 2.5F;
    private static final float FR_MAX_Y = 3.6F;
    private static final float FR_MAX_Z = 3.0F;

    private static final float STAB_MIN_X = -4.0F;
    private static final float STAB_MIN_Y = 0.0F;
    private static final float STAB_MIN_Z = -4.0F;
    private static final float STAB_MAX_X = 4.0F;
    private static final float STAB_MAX_Y = 7.0F;
    private static final float STAB_MAX_Z = 4.0F;

    private static final float RDR_MIN_X = -8.0F;
    private static final float RDR_MIN_Y = 0.0F;
    private static final float RDR_MIN_Z = -8.0F;
    private static final float RDR_MAX_X = 8.0F;
    private static final float RDR_MAX_Y = 3.0F;
    private static final float RDR_MAX_Z = 8.0F;

    private static final float RAD_MIN_X = -7.0F;
    private static final float RAD_MIN_Y = 0.0F;
    private static final float RAD_MIN_Z = -11.0F;
    private static final float RAD_MAX_X = 7.0F;
    private static final float RAD_MAX_Y = 6.0F;
    private static final float RAD_MAX_Z = 8.0F;

    private static final float TEL_MIN_X = -9.0F;
    private static final float TEL_MIN_Y = 0.0F;
    private static final float TEL_MIN_Z = -4.0F;
    private static final float TEL_MAX_X = 9.0F;
    private static final float TEL_MAX_Y = 2.0F;
    private static final float TEL_MAX_Z = 4.0F;

    private static final float CLK_MIN_X = -2.0F;
    private static final float CLK_MIN_Y = 0.0F;
    private static final float CLK_MIN_Z = -3.0F;
    private static final float CLK_MAX_X = 3.0F;
    private static final float CLK_MAX_Y = 8.0F;
    private static final float CLK_MAX_Z = 3.0F;

    private static final float DLK_MIN_X = -11.0F;
    private static final float DLK_MIN_Y = 0.0F;
    private static final float DLK_MIN_Z = -6.0F;
    private static final float DLK_MAX_X = 11.0F;
    private static final float DLK_MAX_Y = 2.0F;
    private static final float DLK_MAX_Z = 8.0F;

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

    private static final float Y_SCALE = 0.8F;
    private static final float PX = 1.0F / 16.0F;
    private static final double REACH = 5.0;

    private static final Map<LookTarget, ConsoleControlSpec> CATALOG = buildCatalog();

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

    /** Spec for {@code target}, or {@code null} for {@link LookTarget#NONE}. */
    public static @Nullable ConsoleControlSpec spec(LookTarget target) {
        if (target == null || target == LookTarget.NONE) {
            return null;
        }
        return CATALOG.get(target);
    }

    /** All interactive control specs (excludes {@link LookTarget#NONE}). */
    public static Collection<ConsoleControlSpec> specs() {
        return Collections.unmodifiableCollection(CATALOG.values());
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
     * Resolves the closest console control along the player's look ray.
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
        for (LookTarget candidate : LookTarget.interactiveValues()) {
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
     * True when the look ray intersects {@code target}'s world AABB.
     * Intended for tests and tooling.
     */
    public static boolean lookHits(
            LookTarget target,
            Direction facing,
            BlockPos pos,
            Vec3 eyePos,
            Vec3 lookDir,
            double reach
    ) {
        if (target == LookTarget.NONE) {
            return false;
        }
        return lookHitsBox(worldBoxFor(target, pos, facing), eyePos, lookDir, reach);
    }

    private static AABB worldBoxFor(LookTarget target, BlockPos pos, Direction facing) {
        return unpaddedBoxFor(target, facing).move(pos.getX(), pos.getY(), pos.getZ());
    }

    private static AABB unpaddedBoxFor(LookTarget target, Direction facing) {
        ConsoleControlSpec layout = spec(target);
        if (layout == null) {
            return new AABB(0, 0, 0, 0, 0, 0);
        }
        return controlBox(facing, layout);
    }

    private static Map<LookTarget, ConsoleControlSpec> buildCatalog() {
        EnumMap<LookTarget, ConsoleControlSpec> map = new EnumMap<>(LookTarget.class);
        put(map, middle(LookTarget.BIOME_SELECTOR, PANEL3_YAW_RAD, SELECTOR_SCALE, BIOME_SELECTOR_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z));
        put(map, middle(LookTarget.WAYPOINT_SELECTOR, PANEL3_YAW_RAD, SELECTOR_SCALE, WAYPOINT_SELECTOR_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z));
        put(map, middle(LookTarget.PLAYER_LOCATOR, PANEL3_YAW_RAD, SELECTOR_SCALE, PLAYER_LOCATOR_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z));
        put(map, middle(LookTarget.PLANET_LOCATOR, PANEL3_YAW_RAD, SELECTOR_SCALE, PLANET_LOCATOR_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z));
        put(map, middle(LookTarget.CHAMELEON_CIRCUIT, PANEL6_YAW_RAD, SELECTOR_SCALE, CHAMELEON_CIRCUIT_MOUNT_X_PX,
                SEL_MIN_X, SEL_MIN_Y, SEL_MIN_Z, SEL_MAX_X, SEL_MAX_Y, SEL_MAX_Z));
        put(map, middle(LookTarget.MATERIALISATION_LEVER, PANEL6_YAW_RAD, LEVER_SCALE, LEVER_MOUNT_X_PX,
                LEV_MIN_X, LEV_MIN_Y, LEV_MIN_Z, LEV_MAX_X, LEV_MAX_Y, LEV_MAX_Z));
        put(map, middle(LookTarget.FAST_RETURN, PANEL6_YAW_RAD, FAST_RETURN_SCALE, FAST_RETURN_MOUNT_X_PX,
                FR_MIN_X, FR_MIN_Y, FR_MIN_Z, FR_MAX_X, FR_MAX_Y, FR_MAX_Z));
        put(map, bottom(LookTarget.STABILISERS, PANEL6_YAW_RAD, STABILISERS_SCALE, STABILISERS_MOUNT_X_PX,
                STAB_MIN_X, STAB_MIN_Y, STAB_MIN_Z, STAB_MAX_X, STAB_MAX_Y, STAB_MAX_Z));
        put(map, middle(LookTarget.OXYGEN_READER, PANEL1_YAW_RAD, READER_SCALE, OXYGEN_READER_MOUNT_X_PX,
                RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z));
        put(map, middle(LookTarget.PRESSURE_READER, PANEL1_YAW_RAD, READER_SCALE, PRESSURE_READER_MOUNT_X_PX,
                RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z));
        put(map, middle(LookTarget.TEMPERATURE_READER, PANEL1_YAW_RAD, READER_SCALE, TEMPERATURE_READER_MOUNT_X_PX,
                RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z));
        put(map, bottom(LookTarget.RADIATION_READER, PANEL1_YAW_RAD, RADIATION_SCALE, 0.0F,
                RAD_MIN_X, RAD_MIN_Y, RAD_MIN_Z, RAD_MAX_X, RAD_MAX_Y, RAD_MAX_Z));
        put(map, middle(LookTarget.REFUELER, PANEL5_YAW_RAD, READER_SCALE, 0.0F,
                RDR_MIN_X, RDR_MIN_Y, RDR_MIN_Z, RDR_MAX_X, RDR_MAX_Y, RDR_MAX_Z));
        put(map, middle(LookTarget.TELEPATHIC_CIRCUIT, PANEL2_YAW_RAD, TELEPATHIC_SCALE, 0.0F,
                TEL_MIN_X, TEL_MIN_Y, TEL_MIN_Z, TEL_MAX_X, TEL_MAX_Y, TEL_MAX_Z));
        put(map, middle(LookTarget.CLOAK, PANEL4_YAW_RAD, CLOAK_SCALE, 0.0F,
                CLK_MIN_X, CLK_MIN_Y, CLK_MIN_Z, CLK_MAX_X, CLK_MAX_Y, CLK_MAX_Z));
        put(map, bottom(LookTarget.DOOR_LOCK, PANEL4_YAW_RAD, DOOR_LOCK_SCALE, 0.0F,
                DLK_MIN_X, DLK_MIN_Y, DLK_MIN_Z, DLK_MAX_X, DLK_MAX_Y, DLK_MAX_Z));
        put(map, bottom(LookTarget.COORDINATE_LOCK_X, PANEL3_YAW_RAD, COORDINATE_LOCK_SCALE, 0.0F,
                CLKX_MIN_X, CLKA_MIN_Y, CLKA_MIN_Z, CLKX_MAX_X, CLKA_MAX_Y, CLKA_MAX_Z));
        put(map, bottom(LookTarget.COORDINATE_LOCK_Y, PANEL3_YAW_RAD, COORDINATE_LOCK_SCALE, 0.0F,
                CLKY_MIN_X, CLKA_MIN_Y, CLKA_MIN_Z, CLKY_MAX_X, CLKA_MAX_Y, CLKA_MAX_Z));
        put(map, bottom(LookTarget.COORDINATE_LOCK_Z, PANEL3_YAW_RAD, COORDINATE_LOCK_SCALE, 0.0F,
                CLKZ_MIN_X, CLKA_MIN_Y, CLKA_MIN_Z, CLKZ_MAX_X, CLKA_MAX_Y, CLKA_MAX_Z));

        for (LookTarget target : LookTarget.interactiveValues()) {
            if (!map.containsKey(target)) {
                throw new IllegalStateException("Missing ConsoleControlSpec for " + target);
            }
        }
        return Map.copyOf(map);
    }

    private static void put(EnumMap<LookTarget, ConsoleControlSpec> map, ConsoleControlSpec spec) {
        map.put(spec.target(), spec);
    }

    private static ConsoleControlSpec middle(
            LookTarget target,
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
        return new ConsoleControlSpec(
                target, yaw, scale, mountX, CONTROL_MOUNT_Y_PX, CONTROL_MOUNT_Z_PX,
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static ConsoleControlSpec bottom(
            LookTarget target,
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
        return new ConsoleControlSpec(
                target, yaw, scale, mountX, BOTTOM_MOUNT_Y_PX, BOTTOM_MOUNT_Z_PX,
                minX, minY, minZ, maxX, maxY, maxZ);
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

    private static AABB controlBox(Direction facing, ConsoleControlSpec layout) {
        double outMinX = Double.POSITIVE_INFINITY;
        double outMinY = Double.POSITIVE_INFINITY;
        double outMinZ = Double.POSITIVE_INFINITY;
        double outMaxX = Double.NEGATIVE_INFINITY;
        double outMaxY = Double.NEGATIVE_INFINITY;
        double outMaxZ = Double.NEGATIVE_INFINITY;

        float[] xs = {layout.minX(), layout.maxX()};
        float[] ys = {layout.minY(), layout.maxY()};
        float[] zs = {layout.minZ(), layout.maxZ()};
        for (float x : xs) {
            for (float y : ys) {
                for (float z : zs) {
                    Vec3 p = controlLocalToBlockLocal(x, y, z, facing, layout);
                    outMinX = Math.min(outMinX, p.x);
                    outMinY = Math.min(outMinY, p.y);
                    outMinZ = Math.min(outMinZ, p.z);
                    outMaxX = Math.max(outMaxX, p.x);
                    outMaxY = Math.max(outMaxY, p.y);
                    outMaxZ = Math.max(outMaxZ, p.z);
                }
            }
        }
        return new AABB(outMinX, outMinY, outMinZ, outMaxX, outMaxY, outMaxZ);
    }

    static Vec3 controlLocalToBlockLocal(
            double px,
            double py,
            double pz,
            Direction facing,
            ConsoleControlSpec layout
    ) {
        return controlLocalToBlockLocal(
                px,
                py,
                pz,
                facing,
                layout.panelYaw(),
                layout.scale(),
                layout.mountX(),
                layout.mountY(),
                layout.mountZ()
        );
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
