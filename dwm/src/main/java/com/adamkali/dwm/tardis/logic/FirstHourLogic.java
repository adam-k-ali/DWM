package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.advancement.DWMCriteria;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Pure eligibility helpers for first-hour teaching advancements.
 */
public final class FirstHourLogic {
    public static final String CLAIMED_OVERLAY_KEY = "dwm.tardis.claimed";

    private FirstHourLogic() {
    }

    /**
     * True when materialising completes a same-world hop that should toast
     * {@code first_hop} (not a Stattenheim summon, not a dimension change).
     */
    public static boolean isSameWorldHop(
            @Nullable String originDimension,
            @Nullable String destinationDimension,
            boolean summonPending
    ) {
        if (summonPending) {
            return false;
        }
        if (originDimension == null || originDimension.isBlank()
                || destinationDimension == null || destinationDimension.isBlank()) {
            return false;
        }
        return originDimension.equals(destinationDimension);
    }

    /** Overlay + claim advancement for a newly claimed ship. */
    public static void notifyClaimed(@Nullable ServerPlayer player) {
        if (player == null) {
            return;
        }
        player.sendOverlayMessage(Component.translatable(CLAIMED_OVERLAY_KEY));
        DWMCriteria.CLAIM_TARDIS.trigger(player);
    }
}
