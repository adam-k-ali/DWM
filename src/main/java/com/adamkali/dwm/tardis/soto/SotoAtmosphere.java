package com.adamkali.dwm.tardis.soto;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * Synced exterior atmosphere for SOTO sky/fog. Single biome sample at the exterior TARDIS pos.
 */
public record SotoAtmosphere(
        Identifier dimensionEffectsId,
        long timeOfDay,
        float rainGradient,
        float thunderGradient,
        int biomeSkyColor,
        int biomeFogColor
) {
    /** Overworld noon-ish fallback when no snapshot atmosphere is available. */
    public static final SotoAtmosphere DEFAULT = new SotoAtmosphere(
            BuiltinDimensionTypes.OVERWORLD.identifier(),
            6000L,
            0.0f,
            0.0f,
            0x78A7FF,
            0xC0D8FF
    );

    public SotoAtmosphere {
        if (dimensionEffectsId == null) {
            dimensionEffectsId = BuiltinDimensionTypes.OVERWORLD.identifier();
        }
        rainGradient = clamp01(rainGradient);
        thunderGradient = clamp01(thunderGradient);
    }

    private static float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }
}
