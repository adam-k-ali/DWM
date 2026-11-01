package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import net.minecraft.world.level.MoonPhase;

/**
 * Pure sky parameters for the SOTO portal, derived from a synced {@link SotoAtmosphere}.
 * <p>
 * Mirrors the fields vanilla's {@code SkyRenderState} feeds to {@code SkyRenderer}, so the
 * client renderer only has to forward them. Angles are in radians.
 */
public record SotoSkyState(
        EffectsKind kind,
        int skyColor,
        float sunAngle,
        float moonAngle,
        float starAngle,
        MoonPhase moonPhase,
        float rainBrightness,
        float starBrightness,
        int sunriseSunsetColor
) {
    /** Only the overworld has a sun, moon, stars and a sunrise glow. */
    public boolean hasCelestials() {
        return kind == EffectsKind.OVERWORLD;
    }

    public static SotoSkyState from(SotoAtmosphere atmosphere) {
        SotoAtmosphere resolved = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        EffectsKind kind = SotoAtmosphereColors.effectsKind(resolved.dimensionEffectsId());
        long time = resolved.timeOfDay();
        float skyAngle = SotoAtmosphereColors.skyAngle(time);
        float sunAngle = SotoAtmosphereColors.skyAngleRadians(time);
        MoonPhase[] phases = MoonPhase.values();
        MoonPhase moonPhase = phases[Math.floorMod(SotoAtmosphereColors.moonPhase(time), phases.length)];
        return new SotoSkyState(
                kind,
                SotoAtmosphereColors.skyColor(
                        resolved.biomeSkyColor(), skyAngle, resolved.rainGradient(), resolved.thunderGradient()),
                sunAngle,
                sunAngle + (float) Math.PI,
                sunAngle,
                moonPhase,
                1.0F - resolved.rainGradient(),
                SotoAtmosphereColors.starBrightness(skyAngle),
                SotoAtmosphereColors.isSunRisingOrSetting(skyAngle)
                        ? SotoAtmosphereColors.sunriseSunsetColor(skyAngle)
                        : 0
        );
    }
}
