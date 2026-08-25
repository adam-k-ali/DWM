package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Per-TARDIS circuit fitted flags: missing/null/true means working; false means broken.
 * Found worldgen ships start unfinished; creative/{@code /give}/legacy saves stay fully fitted.
 */
public final class CircuitFittedLogic {
    public static final String CIRCUIT_BROKEN_KEY = "dwm.console.circuit_broken";

    private static final int SMOKE_COUNT = 8;
    private static final double SMOKE_SPREAD = 0.12;
    private static final double SMOKE_SPEED = 0.02;

    private CircuitFittedLogic() {
    }

    /**
     * {@code null} (legacy Gson) and {@code true} both mean the circuit works.
     */
    public static boolean isFitted(@Nullable TardisDataModel model, @Nullable TardisCircuit circuit) {
        if (model == null || circuit == null) {
            return true;
        }
        Boolean flag = flagOf(model, circuit);
        return flag == null || flag;
    }

    public static boolean isBroken(@Nullable TardisDataModel model, @Nullable TardisCircuit circuit) {
        return !isFitted(model, circuit);
    }

    public static void setFitted(@Nullable TardisDataModel model, @Nullable TardisCircuit circuit, boolean fitted) {
        if (model == null || circuit == null) {
            return;
        }
        writeFlag(model, circuit, fitted);
        model.setChanged();
    }

    /** All circuits working; stabilisers on. Used for {@code /give} and new player-placed ships. */
    public static void applyFullyFitted(@Nullable TardisDataModel model) {
        if (model == null) {
            return;
        }
        for (TardisCircuit circuit : TardisCircuit.values()) {
            writeFlag(model, circuit, true);
        }
        model.stabilisersEnabled = Boolean.TRUE;
        model.setChanged();
    }

    /**
     * Found Type 40 profile: listed circuits broken, stabilisers off.
     * Biome dial, materialisation lever, readers, and door lock stay usable.
     */
    public static void applyFoundUnfinished(@Nullable TardisDataModel model) {
        if (model == null) {
            return;
        }
        for (TardisCircuit circuit : TardisCircuit.values()) {
            writeFlag(model, circuit, false);
        }
        model.stabilisersEnabled = Boolean.FALSE;
        model.setChanged();
    }

    /**
     * Maps a console look target to its circuit, or empty when the control is always available.
     */
    public static java.util.Optional<TardisCircuit> circuitFor(LookTarget target) {
        if (target == null) {
            return java.util.Optional.empty();
        }
        return switch (target) {
            case PLANET_LOCATOR -> java.util.Optional.of(TardisCircuit.PLANET_LOCATOR);
            case WAYPOINT_SELECTOR -> java.util.Optional.of(TardisCircuit.WAYPOINTS);
            case PLAYER_LOCATOR -> java.util.Optional.of(TardisCircuit.PLAYER_LOCATOR);
            case TELEPATHIC_CIRCUIT -> java.util.Optional.of(TardisCircuit.TELEPATHIC);
            case FAST_RETURN -> java.util.Optional.of(TardisCircuit.FAST_RETURN);
            case CLOAK -> java.util.Optional.of(TardisCircuit.CLOAK);
            case CHAMELEON_CIRCUIT -> java.util.Optional.of(TardisCircuit.CHAMELEON);
            case COORDINATE_LOCK_X, COORDINATE_LOCK_Y, COORDINATE_LOCK_Z ->
                    java.util.Optional.of(TardisCircuit.COORDINATE_LOCKS);
            case STABILISERS -> java.util.Optional.of(TardisCircuit.STABILISERS);
            case BIOME_SELECTOR, MATERIALISATION_LEVER, OXYGEN_READER, PRESSURE_READER,
                 TEMPERATURE_READER, RADIATION_READER, REFUELER, DOOR_LOCK, NONE ->
                    java.util.Optional.empty();
        };
    }

    /**
     * Overlay + smoke for a broken console control. No click sound.
     *
     * @return {@code true} when the control was refused as broken
     */
    public static boolean refuseBrokenConsole(
            @Nullable TardisDataModel model,
            LookTarget target,
            Player player,
            Level world,
            BlockPos consolePos,
            Direction facing
    ) {
        java.util.Optional<TardisCircuit> circuit = circuitFor(target);
        if (circuit.isEmpty() || isFitted(model, circuit.get())) {
            return false;
        }
        player.sendOverlayMessage(Component.translatable(CIRCUIT_BROKEN_KEY));
        if (world instanceof ServerLevel serverLevel) {
            AABB box = FirstDoctorConsoleControls.worldBoxForTarget(target, consolePos, facing);
            Vec3 center = box.getCenter();
            spawnBrokenSmoke(serverLevel, center.x, center.y, center.z);
        }
        return true;
    }

    /**
     * Overlay + smoke at a world position (remote, exterior chameleon GUI).
     */
    public static void refuseBrokenAt(
            Player player,
            @Nullable ServerLevel world,
            double x,
            double y,
            double z
    ) {
        player.sendOverlayMessage(Component.translatable(CIRCUIT_BROKEN_KEY));
        if (world != null) {
            spawnBrokenSmoke(world, x, y, z);
        }
    }

    public static void refuseBrokenAtBlock(Player player, @Nullable ServerLevel world, BlockPos pos) {
        refuseBrokenAt(
                player,
                world,
                pos.getX() + 0.5,
                pos.getY() + 0.75,
                pos.getZ() + 0.5
        );
    }

    public static void spawnBrokenSmoke(ServerLevel world, double x, double y, double z) {
        world.sendParticles(
                ParticleTypes.SMOKE,
                x,
                y,
                z,
                SMOKE_COUNT,
                SMOKE_SPREAD,
                SMOKE_SPREAD,
                SMOKE_SPREAD,
                SMOKE_SPEED
        );
        world.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                x,
                y,
                z,
                2,
                SMOKE_SPREAD,
                SMOKE_SPREAD,
                SMOKE_SPREAD,
                SMOKE_SPEED
        );
    }

    /** Pure: smoke spawn origin for a console control box center (testable without a world). */
    public static Vec3 smokeOriginForControl(LookTarget target, BlockPos consolePos, Direction facing) {
        return FirstDoctorConsoleControls.worldBoxForTarget(target, consolePos, facing).getCenter();
    }

    private static @Nullable Boolean flagOf(TardisDataModel model, TardisCircuit circuit) {
        return switch (circuit) {
            case PLANET_LOCATOR -> model.planetLocatorFitted;
            case WAYPOINTS -> model.waypointsFitted;
            case PLAYER_LOCATOR -> model.playerLocatorFitted;
            case TELEPATHIC -> model.telepathicFitted;
            case FAST_RETURN -> model.fastReturnFitted;
            case CLOAK -> model.cloakFitted;
            case CHAMELEON -> model.chameleonFitted;
            case COORDINATE_LOCKS -> model.coordinateLocksFitted;
            case STABILISERS -> model.stabilisersFitted;
            case REMOTE_SUMMON -> model.remoteSummonFitted;
        };
    }

    private static void writeFlag(TardisDataModel model, TardisCircuit circuit, boolean fitted) {
        Boolean value = fitted;
        switch (circuit) {
            case PLANET_LOCATOR -> model.planetLocatorFitted = value;
            case WAYPOINTS -> model.waypointsFitted = value;
            case PLAYER_LOCATOR -> model.playerLocatorFitted = value;
            case TELEPATHIC -> model.telepathicFitted = value;
            case FAST_RETURN -> model.fastReturnFitted = value;
            case CLOAK -> model.cloakFitted = value;
            case CHAMELEON -> model.chameleonFitted = value;
            case COORDINATE_LOCKS -> model.coordinateLocksFitted = value;
            case STABILISERS -> model.stabilisersFitted = value;
            case REMOTE_SUMMON -> model.remoteSummonFitted = value;
        }
    }
}
