package com.adamkali.dwm.tardis.boti;

import net.minecraft.nbt.CompoundTag;

/**
 * One entity sampled from a TARDIS interior footprint for BOTI sync.
 * Pose is relative to the plot origin; NBT includes type {@code id} for client reconstruction.
 */
public record BotiEntitySample(
        float relX,
        float relY,
        float relZ,
        float yaw,
        float pitch,
        CompoundTag nbt
) {
    /** GameProfile UUID for player samples ({@code EntityTypes.PLAYER} is not saveable). */
    public static final String BOTI_PROFILE_ID = "BotiProfileId";
    /** GameProfile name for player samples. */
    public static final String BOTI_PROFILE_NAME = "BotiProfileName";

    public BotiEntitySample {
        nbt = nbt == null ? new CompoundTag() : nbt.copy();
    }
}
