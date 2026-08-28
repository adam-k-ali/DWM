package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Client-facing snapshot of First Doctor console instruments.
 * Built from {@link TardisDataModel} plus an exterior environment {@link ExteriorEnvironmentReadout.Reading}.
 */
public record ConsoleDisplayState(
        TardisChameleonVariant variant,
        boolean stabilisersEnabled,
        boolean cloaked,
        boolean doorsLocked,
        boolean lockX,
        boolean lockY,
        boolean lockZ,
        ExteriorEnvironmentReadout.Reading reading,
        int artron
) {
    public ConsoleDisplayState {
        variant = variant == null ? TardisChameleonVariant.TT_CAPSULE : variant;
        reading = reading == null ? ExteriorEnvironmentReadout.Reading.none() : reading;
        artron = Mth.clamp(artron, 0, ArtronLogic.CAPACITY);
    }

    public static ConsoleDisplayState defaults() {
        return new ConsoleDisplayState(
                TardisChameleonVariant.TT_CAPSULE,
                true,
                false,
                false,
                false,
                false,
                false,
                ExteriorEnvironmentReadout.Reading.none(),
                ArtronLogic.CAPACITY
        );
    }

    public static ConsoleDisplayState from(
            @Nullable TardisDataModel model,
            ExteriorEnvironmentReadout.Reading reading
    ) {
        ExteriorEnvironmentReadout.Reading safeReading =
                reading == null ? ExteriorEnvironmentReadout.Reading.none() : reading;
        if (model == null) {
            return new ConsoleDisplayState(
                    TardisChameleonVariant.TT_CAPSULE,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    safeReading,
                    ArtronLogic.CAPACITY
            );
        }
        return new ConsoleDisplayState(
                model.variant == null ? TardisChameleonVariant.TT_CAPSULE : model.variant,
                StabiliserLogic.isEnabled(model),
                model.cloaked,
                model.doorsLocked,
                model.lockX,
                model.lockY,
                model.lockZ,
                safeReading,
                ArtronLogic.read(model)
        );
    }

    public ConsoleDisplayState withReading(ExteriorEnvironmentReadout.Reading nextReading) {
        return new ConsoleDisplayState(
                variant,
                stabilisersEnabled,
                cloaked,
                doorsLocked,
                lockX,
                lockY,
                lockZ,
                nextReading == null ? ExteriorEnvironmentReadout.Reading.none() : nextReading,
                artron
        );
    }

    public void write(ValueOutput output) {
        output.putString("syncedVariant", variant.getId().toString());
        output.putBoolean("syncedStabilisersEnabled", stabilisersEnabled);
        output.putBoolean("syncedCloaked", cloaked);
        output.putBoolean("syncedDoorsLocked", doorsLocked);
        output.putBoolean("syncedLockX", lockX);
        output.putBoolean("syncedLockY", lockY);
        output.putBoolean("syncedLockZ", lockZ);
        boolean noSignal = reading.noSignal();
        output.putBoolean("syncedNoSignal", noSignal);
        output.putFloat("syncedOxygen", noSignal ? 0.0F : reading.oxygen());
        output.putFloat("syncedPressure", noSignal ? 0.0F : reading.pressure());
        output.putFloat("syncedTemperature", noSignal ? 0.0F : reading.temperature());
        output.putFloat("syncedRadiation", noSignal ? 0.0F : reading.radiation());
        output.putInt("syncedArtron", artron);
    }

    public static ConsoleDisplayState read(ValueInput input) {
        boolean noSignal = input.getBooleanOr("syncedNoSignal", true);
        ExteriorEnvironmentReadout.Reading reading = noSignal
                ? ExteriorEnvironmentReadout.Reading.none()
                : new ExteriorEnvironmentReadout.Reading(
                        false,
                        input.getFloatOr("syncedOxygen", 0.0F),
                        input.getFloatOr("syncedPressure", 0.0F),
                        input.getFloatOr("syncedTemperature", 0.0F),
                        input.getFloatOr("syncedRadiation", 0.0F)
                );
        return new ConsoleDisplayState(
                parseVariant(input.getStringOr("syncedVariant", "")),
                input.getBooleanOr("syncedStabilisersEnabled", true),
                input.getBooleanOr("syncedCloaked", false),
                input.getBooleanOr("syncedDoorsLocked", false),
                input.getBooleanOr("syncedLockX", false),
                input.getBooleanOr("syncedLockY", false),
                input.getBooleanOr("syncedLockZ", false),
                reading,
                input.getIntOr("syncedArtron", ArtronLogic.CAPACITY)
        );
    }

    private static TardisChameleonVariant parseVariant(String id) {
        if (id == null || id.isBlank()) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        try {
            return TardisChameleonVariant.fromId(identifier);
        } catch (IllegalArgumentException ignored) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsoleDisplayState other)) {
            return false;
        }
        return stabilisersEnabled == other.stabilisersEnabled
                && cloaked == other.cloaked
                && doorsLocked == other.doorsLocked
                && lockX == other.lockX
                && lockY == other.lockY
                && lockZ == other.lockZ
                && artron == other.artron
                && variant == other.variant
                && Objects.equals(reading, other.reading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                variant, stabilisersEnabled, cloaked, doorsLocked, lockX, lockY, lockZ, reading, artron);
    }
}
