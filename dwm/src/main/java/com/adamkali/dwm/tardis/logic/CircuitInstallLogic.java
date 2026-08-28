package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.advancement.DWMCriteria;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.item.ConsoleCircuitItem;
import com.adamkali.dwm.item.StattenheimRemoteItem;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Owner-only circuit install: matching console control, or remote-summon with a
 * Stattenheim remote in the other hand.
 */
public final class CircuitInstallLogic {
    public static final String MISMATCH_KEY = "dwm.console.circuit_mismatch";
    public static final String ALREADY_FITTED_KEY = "dwm.console.circuit_already_fitted";
    public static final String INSTALLED_KEY = "dwm.console.circuit_installed";

    public enum Result {
        NOT_OWNER,
        WRONG_TARGET,
        ALREADY_FITTED,
        INSTALLED
    }

    private CircuitInstallLogic() {
    }

    public static @Nullable TardisCircuit circuitOf(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof ConsoleCircuitItem item) {
            return item.circuit();
        }
        return null;
    }

    public static boolean isCircuitItem(@Nullable ItemStack stack) {
        return circuitOf(stack) != null;
    }

    public static boolean otherHandIsRemote(@Nullable Player player, @Nullable InteractionHand hand) {
        if (player == null || hand == null) {
            return false;
        }
        InteractionHand other = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        return player.getItemInHand(other).getItem() instanceof StattenheimRemoteItem;
    }

    /**
     * Pure: decide the install outcome for a circuit used on a console control.
     * Remote summon matches only when {@code otherHandIsRemote} is true (no LookTarget).
     */
    public static Result evaluateConsole(
            @Nullable TardisDataModel model,
            @Nullable UUID playerUuid,
            @Nullable TardisCircuit held,
            @Nullable LookTarget target,
            boolean otherHandIsRemote
    ) {
        if (held == null) {
            return Result.WRONG_TARGET;
        }
        if (!ConsolePilotLogic.canInstallCircuit(model, playerUuid)) {
            return Result.NOT_OWNER;
        }
        if (!matchesConsole(held, target, otherHandIsRemote)) {
            return Result.WRONG_TARGET;
        }
        if (CircuitFittedLogic.isFitted(model, held)) {
            return Result.ALREADY_FITTED;
        }
        return Result.INSTALLED;
    }

    /**
     * Pure: remote-summon item use while a Stattenheim remote is (or is not) in the other hand.
     */
    public static Result evaluateRemote(
            @Nullable TardisDataModel model,
            @Nullable UUID playerUuid,
            @Nullable TardisCircuit held,
            boolean otherHandIsRemote
    ) {
        if (held != TardisCircuit.REMOTE_SUMMON || !otherHandIsRemote) {
            return Result.WRONG_TARGET;
        }
        if (model == null || !ConsolePilotLogic.canInstallCircuit(model, playerUuid)) {
            return Result.NOT_OWNER;
        }
        if (CircuitFittedLogic.isFitted(model, TardisCircuit.REMOTE_SUMMON)) {
            return Result.ALREADY_FITTED;
        }
        return Result.INSTALLED;
    }

    public static boolean matchesConsole(
            @Nullable TardisCircuit held,
            @Nullable LookTarget target,
            boolean otherHandIsRemote
    ) {
        if (held == null) {
            return false;
        }
        if (held == TardisCircuit.REMOTE_SUMMON) {
            return otherHandIsRemote;
        }
        Optional<TardisCircuit> mapped = CircuitFittedLogic.circuitFor(target);
        return mapped.isPresent() && mapped.get() == held;
    }

    public static String controlTranslationKey(@Nullable LookTarget target) {
        if (target == null) {
            return "dwm.console.circuit_mismatch";
        }
        return switch (target) {
            case PLANET_LOCATOR -> "dwm.console.planet_locator";
            case WAYPOINT_SELECTOR -> "dwm.console.waypoint_selector";
            case PLAYER_LOCATOR -> "dwm.console.player_locator";
            case TELEPATHIC_CIRCUIT -> "dwm.console.telepathic_circuit";
            case FAST_RETURN -> "dwm.console.fast_return";
            case CLOAK -> "dwm.console.cloak";
            case CHAMELEON_CIRCUIT -> "dwm.console.chameleon_circuit";
            case COORDINATE_LOCK_X, COORDINATE_LOCK_Y, COORDINATE_LOCK_Z -> "dwm.console.coordinate_locks";
            case STABILISERS -> "dwm.console.stabilisers";
            case BIOME_SELECTOR -> "dwm.console.biome_selector";
            case MATERIALISATION_LEVER -> "dwm.console.materialisation_lever";
            case OXYGEN_READER -> "dwm.console.oxygen_reader";
            case PRESSURE_READER -> "dwm.console.pressure_reader";
            case TEMPERATURE_READER -> "dwm.console.temperature_reader";
            case RADIATION_READER -> "dwm.console.radiation_reader";
            case REFUELER -> "dwm.console.refueler";
            case DOOR_LOCK -> "dwm.console.door_lock";
            case NONE -> "dwm.console.circuit_mismatch";
        };
    }

    public static Component controlName(@Nullable LookTarget target) {
        return Component.translatable(controlTranslationKey(target));
    }

    public static void apply(
            Result result,
            Player player,
            ItemStack stack,
            TardisCircuit held,
            Component targetName,
            @Nullable TardisDataModel model
    ) {
        Component circuitName = Component.translatable(held.translationKey());
        switch (result) {
            case NOT_OWNER -> player.sendOverlayMessage(Component.translatable(ConsolePilotLogic.NOT_OWNER_KEY));
            case WRONG_TARGET -> player.sendOverlayMessage(
                    Component.translatable(MISMATCH_KEY, circuitName, targetName)
            );
            case ALREADY_FITTED -> player.sendOverlayMessage(
                    Component.translatable(ALREADY_FITTED_KEY, circuitName)
            );
            case INSTALLED -> {
                CircuitFittedLogic.setFitted(model, held, true);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.sendOverlayMessage(Component.translatable(INSTALLED_KEY, circuitName));
                if (player instanceof ServerPlayer serverPlayer) {
                    DWMCriteria.FIRST_CIRCUIT.trigger(serverPlayer);
                }
            }
        }
    }
}
