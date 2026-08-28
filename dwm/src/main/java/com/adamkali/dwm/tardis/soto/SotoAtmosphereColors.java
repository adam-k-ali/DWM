package com.adamkali.dwm.tardis.soto;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Pure atmosphere color helpers mirroring essential vanilla ClientWorld / DimensionEffects math
 * without requiring a ClientWorld.
 */
public final class SotoAtmosphereColors {
    public enum EffectsKind {
        OVERWORLD,
        NETHER,
        END
    }

    private SotoAtmosphereColors() {
    }

    public static EffectsKind effectsKind(Identifier dimensionEffectsId) {
        if (BuiltinDimensionTypes.NETHER.identifier().equals(dimensionEffectsId)) {
            return EffectsKind.NETHER;
        }
        if (BuiltinDimensionTypes.END.identifier().equals(dimensionEffectsId)) {
            return EffectsKind.END;
        }
        return EffectsKind.OVERWORLD;
    }

    /**
     * Same curve as {@code DimensionType.getSkyAngle(long)} without fixed-time override.
     */
    public static float skyAngle(long timeOfDay) {
        double d = Mth.frac((double) timeOfDay / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) (d * 2.0 + e) / 3.0F;
    }

    public static float skyAngleRadians(long timeOfDay) {
        return skyAngle(timeOfDay) * ((float) Math.PI * 2.0F);
    }

    public static int moonPhase(long timeOfDay) {
        return (int) (timeOfDay / 24000L % 8L + 8L) % 8;
    }

    /** Sun height factor used by overworld fog darkening (0 at night, 1 at noon). */
    public static float sunHeight(float skyAngle) {
        float cos = Mth.cos(skyAngle * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F;
        return Mth.clamp(cos, 0.0F, 1.0F);
    }

    /**
     * Mirrors {@code ClientWorld.getSkyColor} without cubic biome blend or lightning.
     */
    public static int skyColor(int biomeSkyColor, float skyAngle, float rainGradient, float thunderGradient) {
        var rgb = ARGB.vector3fFromRGB24(biomeSkyColor);
        Vec3 base = new Vec3(rgb.x, rgb.y, rgb.z).scale(sunHeight(skyAngle));
        int color = ARGB.color(base);
        if (rainGradient > 0.0F) {
            int gray = ARGB.scaleRGB(ARGB.greyscale(color), 0.6F);
            color = ARGB.srgbLerp(rainGradient * 0.75F, color, gray);
        }
        if (thunderGradient > 0.0F) {
            int gray = ARGB.scaleRGB(ARGB.greyscale(color), 0.2F);
            color = ARGB.srgbLerp(thunderGradient * 0.75F, color, gray);
        }
        return color;
    }

    /**
     * Mirrors essential {@code BackgroundRenderer.getFogColor} + DimensionEffects.adjustFogColor
     * for air (no water/lava submersion).
     */
    public static Vec3 fogColor(
            int biomeFogColor,
            EffectsKind kind,
            float skyAngle,
            float rainGradient,
            float thunderGradient
    ) {
        var rgb = ARGB.vector3fFromRGB24(biomeFogColor);
        Vec3 color = new Vec3(rgb.x, rgb.y, rgb.z);
        float height = sunHeight(skyAngle);
        color = adjustFogColor(color, kind, height);

        if (rainGradient > 0.0F) {
            float g = 1.0F - rainGradient * 0.5F;
            float h = 1.0F - rainGradient * 0.4F;
            color = new Vec3(color.x * g, color.y * g, color.z * h);
        }
        if (thunderGradient > 0.0F) {
            float g = 1.0F - thunderGradient * 0.5F;
            color = color.scale(g);
        }
        return color;
    }

    public static Vec3 adjustFogColor(Vec3 color, EffectsKind kind, float sunHeight) {
        return switch (kind) {
            case END -> color.scale(0.15F);
            case NETHER -> color;
            case OVERWORLD -> color.multiply(
                    sunHeight * 0.94F + 0.06F,
                    sunHeight * 0.94F + 0.06F,
                    sunHeight * 0.91F + 0.09F
            );
        };
    }

    /** Mirrors {@code ClientWorld.getStarBrightness}. */
    public static float starBrightness(float skyAngle) {
        float g = 1.0F - (Mth.cos(skyAngle * ((float) Math.PI * 2.0F)) * 2.0F + 0.25F);
        g = Mth.clamp(g, 0.0F, 1.0F);
        return g * g * 0.5F;
    }

    public static boolean isSunRisingOrSetting(float skyAngle) {
        float f = Mth.cos(skyAngle * ((float) Math.PI * 2.0F));
        return f >= -0.4F && f <= 0.4F;
    }

    /** Sunrise/sunset band color from DimensionEffects.Overworld.getSkyColor. */
    public static int sunriseSunsetColor(float skyAngle) {
        float f = Mth.cos(skyAngle * ((float) Math.PI * 2.0F));
        float g = f / 0.4F * 0.5F + 0.5F;
        float h = Mth.square(1.0F - (1.0F - Mth.sin(g * (float) Math.PI)) * 0.99F);
        return ARGB.colorFromFloat(h, g * 0.3F + 0.7F, g * g * 0.7F + 0.2F, 0.2F);
    }

    /**
     * Overworld night floor from {@code minecraft:visual/sky_light_factor} (day timeline).
     * Nether/End keep factor 0 so packed sky light does not wash out ambient.
     */
    public static final float NIGHT_SKY_LIGHT_FACTOR = 0.24F;
    public static final int OVERWORLD_AMBIENT_LIGHT_COLOR = 0x0A0A0A;
    public static final int NETHER_AMBIENT_LIGHT_COLOR = 0x302821;
    public static final int END_AMBIENT_LIGHT_COLOR = 0x3F473F;

    /**
     * Lightmap {@code skyFactor}: overworld follows sun height with a 0.24 night floor,
     * weather-blended toward that floor. Nether/End stay unlit by sky.
     */
    public static float skyLightFactor(EffectsKind kind, float skyAngle, float rainGradient, float thunderGradient) {
        if (kind != EffectsKind.OVERWORLD) {
            return 0.0F;
        }
        float factor = NIGHT_SKY_LIGHT_FACTOR + (1.0F - NIGHT_SKY_LIGHT_FACTOR) * sunHeight(skyAngle);
        float weather = Math.max(clamp01(rainGradient) * 0.3125F, clamp01(thunderGradient) * 0.52734375F);
        return Mth.lerp(weather, factor, NIGHT_SKY_LIGHT_FACTOR);
    }

    public static int ambientLightColor(EffectsKind kind) {
        return switch (kind) {
            case NETHER -> NETHER_AMBIENT_LIGHT_COLOR;
            case END -> END_AMBIENT_LIGHT_COLOR;
            case OVERWORLD -> OVERWORLD_AMBIENT_LIGHT_COLOR;
        };
    }

    public static int skyLightColor(EffectsKind kind) {
        return switch (kind) {
            case NETHER -> 0x7A7AFF;
            case END -> 0xAC60CD;
            case OVERWORLD -> 0xFFFFFF;
        };
    }

    private static float clamp01(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }
}
