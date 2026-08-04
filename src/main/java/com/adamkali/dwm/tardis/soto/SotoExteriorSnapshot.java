package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;

import java.util.UUID;

/**
 * Server-built exterior SOTO snapshot.
 * formatVersion 5 = shell metadata + atmosphere (terrain/entities come from the ghost stream).
 */
public record SotoExteriorSnapshot(
        int formatVersion,
        UUID tardisId,
        int revision,
        TardisChameleonVariant variant,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation,
        SotoAtmosphere atmosphere
) {
    /** @deprecated Prefer {@link #FORMAT_VERSION_SHELL_AND_ATMOSPHERE}. */
    @Deprecated
    public static final int FORMAT_VERSION_BLOCKS_BES_ENTITIES_SHELL = 3;
    /** @deprecated Prefer {@link #FORMAT_VERSION_SHELL_AND_ATMOSPHERE}. */
    @Deprecated
    public static final int FORMAT_VERSION_ATMOSPHERE = 4;
    public static final int FORMAT_VERSION_SHELL_AND_ATMOSPHERE = 5;

    public SotoExteriorSnapshot {
        if (variant == null) {
            variant = TardisChameleonVariant.TT_CAPSULE;
        }
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
    }

    public static SotoExteriorSnapshot of(
            UUID tardisId,
            int revision,
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation,
            SotoAtmosphere atmosphere
    ) {
        return new SotoExteriorSnapshot(
                FORMAT_VERSION_SHELL_AND_ATMOSPHERE,
                tardisId,
                revision,
                variant,
                doorSwing,
                isOpen,
                exteriorRotation,
                atmosphere
        );
    }
}
