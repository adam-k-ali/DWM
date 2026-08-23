package com.adamkali.dwm.tardis.portal;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * Synced sky/fog atmosphere for portal look-through (sampled from the looked-into world).
 */
public record PortalAtmosphere(
        Identifier dimensionEffectsId,
        long timeOfDay,
        float rainGradient,
        float thunderGradient,
        int biomeSkyColor,
        int biomeFogColor
) {
    /** Overworld noon-ish fallback when no snapshot atmosphere is available. */
    public static final PortalAtmosphere DEFAULT = new PortalAtmosphere(
            BuiltinDimensionTypes.OVERWORLD.identifier(),
            6000L,
            0.0f,
            0.0f,
            0x78A7FF,
            0xC0D8FF
    );

    public PortalAtmosphere {
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
