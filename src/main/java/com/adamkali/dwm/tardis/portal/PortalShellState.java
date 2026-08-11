package com.adamkali.dwm.tardis.portal;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;

/**
 * Synced exterior shell metadata shared by BOTI and SOTO portal streams.
 */
public record PortalShellState(
        TardisChameleonVariant variant,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation
) {
    public PortalShellState {
        if (variant == null) {
            variant = TardisChameleonVariant.TT_CAPSULE;
        }
    }
}
