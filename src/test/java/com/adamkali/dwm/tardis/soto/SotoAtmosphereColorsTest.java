package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SotoAtmosphereColorsTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void effectsKind_mapsVanillaIds() {
        assertEquals(SotoAtmosphereColors.EffectsKind.OVERWORLD,
                SotoAtmosphereColors.effectsKind(DimensionTypes.OVERWORLD_ID));
        assertEquals(SotoAtmosphereColors.EffectsKind.NETHER,
                SotoAtmosphereColors.effectsKind(DimensionTypes.THE_NETHER_ID));
        assertEquals(SotoAtmosphereColors.EffectsKind.END,
                SotoAtmosphereColors.effectsKind(DimensionTypes.THE_END_ID));
    }

    @Test
    void skyColor_dayIsBrighterThanNight() {
        int biomeSky = 0x78A7FF;
        int day = SotoAtmosphereColors.skyColor(biomeSky, SotoAtmosphereColors.skyAngle(6000L), 0.0f, 0.0f);
        int night = SotoAtmosphereColors.skyColor(biomeSky, SotoAtmosphereColors.skyAngle(18000L), 0.0f, 0.0f);
        int dayLum = ColorHelper.getRed(day) + ColorHelper.getGreen(day) + ColorHelper.getBlue(day);
        int nightLum = ColorHelper.getRed(night) + ColorHelper.getGreen(night) + ColorHelper.getBlue(night);
        assertTrue(dayLum > nightLum, "day sky should be brighter than night");
    }

    @Test
    void skyColor_rainDarkens() {
        int biomeSky = 0x78A7FF;
        float skyAngle = SotoAtmosphereColors.skyAngle(6000L);
        int clear = SotoAtmosphereColors.skyColor(biomeSky, skyAngle, 0.0f, 0.0f);
        int rainy = SotoAtmosphereColors.skyColor(biomeSky, skyAngle, 1.0f, 0.0f);
        int clearLum = ColorHelper.getRed(clear) + ColorHelper.getGreen(clear) + ColorHelper.getBlue(clear);
        int rainLum = ColorHelper.getRed(rainy) + ColorHelper.getGreen(rainy) + ColorHelper.getBlue(rainy);
        assertTrue(rainLum < clearLum, "rain should darken sky");
    }

    @Test
    void fogColor_endIsDarkened() {
        Vec3d base = SotoAtmosphereColors.fogColor(
                0xC0D8FF,
                SotoAtmosphereColors.EffectsKind.OVERWORLD,
                SotoAtmosphereColors.skyAngle(6000L),
                0.0f,
                0.0f
        );
        Vec3d end = SotoAtmosphereColors.fogColor(
                0xC0D8FF,
                SotoAtmosphereColors.EffectsKind.END,
                SotoAtmosphereColors.skyAngle(6000L),
                0.0f,
                0.0f
        );
        assertTrue(end.length() < base.length() * 0.5, "end fog should be much darker");
    }

    @Test
    void fogColor_overworldNightDarkerThanDay() {
        Vec3d day = SotoAtmosphereColors.fogColor(
                0xC0D8FF,
                SotoAtmosphereColors.EffectsKind.OVERWORLD,
                SotoAtmosphereColors.skyAngle(6000L),
                0.0f,
                0.0f
        );
        Vec3d night = SotoAtmosphereColors.fogColor(
                0xC0D8FF,
                SotoAtmosphereColors.EffectsKind.OVERWORLD,
                SotoAtmosphereColors.skyAngle(18000L),
                0.0f,
                0.0f
        );
        assertTrue(night.length() < day.length(), "night fog should be darker");
    }

    @Test
    void fogColor_netherUnadjustedBySun() {
        Vec3d day = SotoAtmosphereColors.adjustFogColor(
                Vec3d.unpackRgb(0x330808),
                SotoAtmosphereColors.EffectsKind.NETHER,
                1.0f
        );
        Vec3d night = SotoAtmosphereColors.adjustFogColor(
                Vec3d.unpackRgb(0x330808),
                SotoAtmosphereColors.EffectsKind.NETHER,
                0.0f
        );
        assertEquals(day.x, night.x, 1e-6);
        assertEquals(day.y, night.y, 1e-6);
        assertEquals(day.z, night.z, 1e-6);
    }

    @Test
    void starBrightness_higherAtNight() {
        float day = SotoAtmosphereColors.starBrightness(SotoAtmosphereColors.skyAngle(6000L));
        float night = SotoAtmosphereColors.starBrightness(SotoAtmosphereColors.skyAngle(18000L));
        assertTrue(night > day);
    }

    @Test
    void moonPhase_cyclesWithTime() {
        assertEquals(0, SotoAtmosphereColors.moonPhase(0L));
        assertEquals(1, SotoAtmosphereColors.moonPhase(24000L));
        assertEquals(0, SotoAtmosphereColors.moonPhase(8L * 24000L));
    }
}
