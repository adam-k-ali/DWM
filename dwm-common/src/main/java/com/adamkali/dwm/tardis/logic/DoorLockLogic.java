package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Door lock toggle. Opening is refused while locked; closing is always allowed.
 * Lock and unlock only apply when doors are fully closed.
 */
public final class DoorLockLogic {
    private DoorLockLogic() {
    }

    public static boolean isLocked(@Nullable TardisDataModel model) {
        return model != null && model.doorsLocked;
    }

    /** True when doors are fully shut ({@code !isOpen} and swing at rest). */
    public static boolean areDoorsClosed(@Nullable TardisDoorState doorState) {
        return doorState != null && !doorState.isOpen && doorState.doorSwing <= 0.0f;
    }

    public static boolean canToggleLock(@Nullable TardisDataModel model) {
        return model != null && areDoorsClosed(model.doorState);
    }

    /**
     * Flips {@link TardisDataModel#doorsLocked} when doors are fully closed.
     *
     * @return the new locked state, or {@code false} when {@code model} is null.
     *         When doors are not closed, returns the unchanged locked state.
     */
    public static boolean toggle(@Nullable TardisDataModel model) {
        if (model == null) {
            return false;
        }
        if (!canToggleLock(model)) {
            return model.doorsLocked;
        }
        model.doorsLocked = !model.doorsLocked;
        model.setChanged();
        return model.doorsLocked;
    }

    /**
     * Toggles a lock, updates linked console state, and reports the result to the player.
     *
     * @return {@code true} when the interaction was applied
     */
    public static boolean toggleForPlayer(
            @Nullable TardisDataModel model,
            @Nullable Player player,
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId
    ) {
        if (model == null) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("dwm.console.door_lock_unavailable"));
            }
            return false;
        }
        if (!canToggleLock(model)) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("dwm.console.doors_must_be_closed"));
            }
            return false;
        }

        boolean locked = toggle(model);
        FirstDoctorConsoleSync.syncFromModel(server, tardisId);
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(
                    locked ? "dwm.console.doors_locked" : "dwm.console.doors_unlocked"
            ));
        }
        return true;
    }

    /** True when an attempt to open should be refused. */
    public static boolean blocksOpen(@Nullable TardisDataModel model, boolean currentlyOpen) {
        return isLocked(model) && !currentlyOpen;
    }
}
