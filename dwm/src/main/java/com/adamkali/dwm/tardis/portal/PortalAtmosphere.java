package com.adamkali.dwm.tardis.portal;

import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * Synced sky/fog atmosphere for portal look-through (sampled from the looked-into world).
 * <p>
 * The lighting fields are what vanilla feeds the lightmap and block-face shading for a camera
 * standing in that world ({@code visual/sky_light_factor}, {@code visual/sky_light_color},
 * {@code visual/ambient_light_color}, {@code visual/block_light_tint} and the dimension type's
 * {@code cardinal_light}), so the portal is lit as it would be from inside.
 */
public record PortalAtmosphere(
        Identifier dimensionEffectsId,
        long timeOfDay,
        float rainGradient,
        float thunderGradient,
        int biomeSkyColor,
        int biomeFogColor,
        float skyLightFactor,
        int skyLightColor,
        int ambientLightColor,
        int blockLightTint,
        CardinalLighting.Type cardinalLighting
) {
    /** Vanilla {@code visual/block_light_tint} default (warm torch light). */
    public static final int DEFAULT_BLOCK_LIGHT_TINT = 0xFFD88C;

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
        if (cardinalLighting == null) {
            cardinalLighting = CardinalLighting.Type.DEFAULT;
        }
        rainGradient = clamp01(rainGradient);
        thunderGradient = clamp01(thunderGradient);
        skyLightFactor = clamp01(skyLightFactor);
    }

    /**
     * Sky/fog-only atmosphere; lighting is approximated from the vanilla dimension id
     * (unknown ids, including {@code dwm:tardis}, are treated as overworld). Server sampling uses the
     * canonical constructor with the world's real attribute values.
     */
    public PortalAtmosphere(
            Identifier dimensionEffectsId,
            long timeOfDay,
            float rainGradient,
            float thunderGradient,
            int biomeSkyColor,
            int biomeFogColor
    ) {
        this(
                dimensionEffectsId,
                timeOfDay,
                rainGradient,
                thunderGradient,
                biomeSkyColor,
                biomeFogColor,
                derivedSkyLightFactor(dimensionEffectsId, timeOfDay, rainGradient, thunderGradient),
                SotoAtmosphereColors.skyLightColor(SotoAtmosphereColors.effectsKind(dimensionEffectsId)),
                SotoAtmosphereColors.ambientLightColor(SotoAtmosphereColors.effectsKind(dimensionEffectsId)),
                DEFAULT_BLOCK_LIGHT_TINT,
                SotoAtmosphereColors.effectsKind(dimensionEffectsId) == EffectsKind.NETHER
                        ? CardinalLighting.Type.NETHER
                        : CardinalLighting.Type.DEFAULT
        );
    }

    private static float derivedSkyLightFactor(
            Identifier dimensionEffectsId,
            long timeOfDay,
            float rainGradient,
            float thunderGradient
    ) {
        return SotoAtmosphereColors.skyLightFactor(
                SotoAtmosphereColors.effectsKind(dimensionEffectsId),
                SotoAtmosphereColors.skyAngle(timeOfDay),
                rainGradient,
                thunderGradient
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
