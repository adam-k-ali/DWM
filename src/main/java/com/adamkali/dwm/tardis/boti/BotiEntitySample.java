package com.adamkali.dwm.tardis.boti;

import net.minecraft.nbt.NbtCompound;

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
        NbtCompound nbt
) {
    /** GameProfile UUID for player samples ({@code EntityType.PLAYER} is not saveable). */
    public static final String BOTI_PROFILE_ID = "BotiProfileId";
    /** GameProfile name for player samples. */
    public static final String BOTI_PROFILE_NAME = "BotiProfileName";

    public BotiEntitySample {
        nbt = nbt == null ? new NbtCompound() : nbt.copy();
    }
}
