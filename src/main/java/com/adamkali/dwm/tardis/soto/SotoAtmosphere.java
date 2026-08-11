package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * @deprecated Prefer {@link PortalAtmosphere}; kept as a thin alias for existing SOTO render helpers.
 */
@Deprecated
public record SotoAtmosphere(
        Identifier dimensionEffectsId,
        long timeOfDay,
        float rainGradient,
        float thunderGradient,
        int biomeSkyColor,
        int biomeFogColor
) {
    public static final SotoAtmosphere DEFAULT = fromPortal(PortalAtmosphere.DEFAULT);

    public SotoAtmosphere {
        if (dimensionEffectsId == null) {
            dimensionEffectsId = BuiltinDimensionTypes.OVERWORLD.identifier();
        }
        rainGradient = clamp01(rainGradient);
        thunderGradient = clamp01(thunderGradient);
    }

    public PortalAtmosphere toPortal() {
        return new PortalAtmosphere(
                dimensionEffectsId, timeOfDay, rainGradient, thunderGradient, biomeSkyColor, biomeFogColor
        );
    }

    public static SotoAtmosphere fromPortal(PortalAtmosphere atmosphere) {
        if (atmosphere == null) {
            return DEFAULT;
        }
        return new SotoAtmosphere(
                atmosphere.dimensionEffectsId(),
                atmosphere.timeOfDay(),
                atmosphere.rainGradient(),
                atmosphere.thunderGradient(),
                atmosphere.biomeSkyColor(),
                atmosphere.biomeFogColor()
        );
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
