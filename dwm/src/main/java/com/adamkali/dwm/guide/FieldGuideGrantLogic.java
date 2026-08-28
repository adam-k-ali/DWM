package com.adamkali.dwm.guide;

/**
 * Pure eligibility and slot choice for the once-per-player Field Guide gift.
 */
public final class FieldGuideGrantLogic {
    /**
     * Last hotbar slot. Keeps the selected slot (usually 0) free so first pickups
     * and {@code /give} land in the hand.
     */
    public static final int PREFERRED_HOTBAR_SLOT = 8;

    /** {@link net.minecraft.world.entity.player.Inventory#add} fallback when preferred slot is taken. */
    public static final int FALLBACK_TO_ADD = -1;

    private FieldGuideGrantLogic() {
    }

    public static boolean shouldGrant(boolean alreadyReceived) {
        return !alreadyReceived;
    }

    /**
     * @return {@link #PREFERRED_HOTBAR_SLOT} when that slot is empty, otherwise {@link #FALLBACK_TO_ADD}
     */
    public static int slotForGrant(boolean preferredSlotEmpty) {
        return preferredSlotEmpty ? PREFERRED_HOTBAR_SLOT : FALLBACK_TO_ADD;
    }
}
