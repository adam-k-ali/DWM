package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.item.DWMDataComponents;
import com.adamkali.dwm.item.TardisKeyItem;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Console permission gates: owner-only piloting, public readers, door lock via owner or bound key.
 */
public final class ConsolePilotLogic {
    public static final String NOT_OWNER_KEY = "dwm.console.not_owner";

    private ConsolePilotLogic() {
    }

    /** Atmosphere readers and the refueler gauge stay usable by anyone. */
    public static boolean isPublicReader(@Nullable LookTarget target) {
        if (target == null) {
            return false;
        }
        return switch (target) {
            case OXYGEN_READER, PRESSURE_READER, TEMPERATURE_READER, RADIATION_READER, REFUELER -> true;
            default -> false;
        };
    }

    /** Travel, destinations, cloak, chameleon, stabilisers — owner only. */
    public static boolean canPilot(@Nullable TardisDataModel model, @Nullable UUID playerUuid) {
        return TardisOwnershipLogic.isOwner(model, playerUuid);
    }

    /**
     * Door lock from the console: owner, or anyone holding a key bound to this TARDIS.
     * Unbound keys do not authorize (binding stays on door use).
     */
    public static boolean canToggleDoorLock(
            @Nullable TardisDataModel model,
            @Nullable UUID playerUuid,
            @Nullable UUID heldBoundTardisId
    ) {
        if (TardisOwnershipLogic.isOwner(model, playerUuid)) {
            return true;
        }
        if (model == null || model.uuid == null) {
            return false;
        }
        return TardisKeyLogic.useOnTardis(heldBoundTardisId, playerUuid, model.uuid, model)
                == TardisKeyLogic.UseResult.TOGGLE_READY;
    }

    /** Circuit install (DWM-060): same as piloting — owner only. */
    public static boolean canInstallCircuit(@Nullable TardisDataModel model, @Nullable UUID playerUuid) {
        return canPilot(model, playerUuid);
    }

    /**
     * Bound TARDIS UUID from a key in main or offhand, or {@code null} if none.
     */
    public static @Nullable UUID heldBoundTardisId(@Nullable Player player) {
        if (player == null) {
            return null;
        }
        UUID main = boundIdFrom(player.getMainHandItem());
        if (main != null) {
            return main;
        }
        return boundIdFrom(player.getOffhandItem());
    }

    private static @Nullable UUID boundIdFrom(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof TardisKeyItem)) {
            return null;
        }
        return stack.get(DWMDataComponents.BOUND_TARDIS_ID);
    }
}
