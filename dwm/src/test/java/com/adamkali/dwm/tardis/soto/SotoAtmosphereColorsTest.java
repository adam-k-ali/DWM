package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
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
                SotoAtmosphereColors.effectsKind(BuiltinDimensionTypes.OVERWORLD.identifier()));
        assertEquals(SotoAtmosphereColors.EffectsKind.NETHER,
                SotoAtmosphereColors.effectsKind(BuiltinDimensionTypes.NETHER.identifier()));
        assertEquals(SotoAtmosphereColors.EffectsKind.END,
                SotoAtmosphereColors.effectsKind(BuiltinDimensionTypes.END.identifier()));
    }

    @Test
    void skyColor_dayIsBrighterThanNight() {
        int biomeSky = 0x78A7FF;
        int day = SotoAtmosphereColors.skyColor(biomeSky, SotoAtmosphereColors.skyAngle(6000L), 0.0f, 0.0f);
        int night = SotoAtmosphereColors.skyColor(biomeSky, SotoAtmosphereColors.skyAngle(18000L), 0.0f, 0.0f);
        int dayLum = ARGB.red(day) + ARGB.green(day) + ARGB.blue(day);
        int nightLum = ARGB.red(night) + ARGB.green(night) + ARGB.blue(night);
        assertTrue(dayLum > nightLum, "day sky should be brighter than night");
    }

    @Test
    void skyColor_rainDarkens() {
        int biomeSky = 0x78A7FF;
        float skyAngle = SotoAtmosphereColors.skyAngle(6000L);
        int clear = SotoAtmosphereColors.skyColor(biomeSky, skyAngle, 0.0f, 0.0f);
        int rainy = SotoAtmosphereColors.skyColor(biomeSky, skyAngle, 1.0f, 0.0f);
        int clearLum = ARGB.red(clear) + ARGB.green(clear) + ARGB.blue(clear);
        int rainLum = ARGB.red(rainy) + ARGB.green(rainy) + ARGB.blue(rainy);
        assertTrue(rainLum < clearLum, "rain should darken sky");
    }

    @Test
    void fogColor_endIsDarkened() {
        Vec3 base = SotoAtmosphereColors.fogColor(
                0xC0D8FF,
                SotoAtmosphereColors.EffectsKind.OVERWORLD,
                SotoAtmosphereColors.skyAngle(6000L),
                0.0f,
                0.0f
        );
        Vec3 end = SotoAtmosphereColors.fogColor(
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
        Vec3 day = SotoAtmosphereColors.fogColor(
                0xC0D8FF,
                SotoAtmosphereColors.EffectsKind.OVERWORLD,
                SotoAtmosphereColors.skyAngle(6000L),
                0.0f,
                0.0f
        );
        Vec3 night = SotoAtmosphereColors.fogColor(
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
        Vec3 day = SotoAtmosphereColors.adjustFogColor(
                new Vec3(ARGB.vector3fFromRGB24(0x330808)),
                SotoAtmosphereColors.EffectsKind.NETHER,
                1.0f
        );
        Vec3 night = SotoAtmosphereColors.adjustFogColor(
                new Vec3(ARGB.vector3fFromRGB24(0x330808)),
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
