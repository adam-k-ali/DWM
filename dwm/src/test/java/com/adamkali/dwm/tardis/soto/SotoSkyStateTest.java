package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SotoSkyStateTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static SotoAtmosphere overworldAt(long time, float rain) {
        return new SotoAtmosphere(
                BuiltinDimensionTypes.OVERWORLD.identifier(), time, rain, 0.0f, 0x78A7FF, 0xC0D8FF);
    }

    @Test
    void from_nullFallsBackToDefault() {
        SotoSkyState state = SotoSkyState.from(null);
        assertEquals(SotoSkyState.from(SotoAtmosphere.DEFAULT), state);
    }

    @Test
    void from_moonOppositeSun() {
        SotoSkyState state = SotoSkyState.from(overworldAt(6000L, 0.0f));
        assertEquals(state.sunAngle() + (float) Math.PI, state.moonAngle(), 1e-6f);
        assertEquals(state.sunAngle(), state.starAngle(), 1e-6f);
    }

    @Test
    void from_starsOnlyAtNight() {
        float day = SotoSkyState.from(overworldAt(6000L, 0.0f)).starBrightness();
        float night = SotoSkyState.from(overworldAt(18000L, 0.0f)).starBrightness();
        assertTrue(night > day);
    }

    @Test
    void from_rainDimsCelestials() {
        assertEquals(1.0f, SotoSkyState.from(overworldAt(6000L, 0.0f)).rainBrightness(), 1e-6f);
        assertEquals(0.25f, SotoSkyState.from(overworldAt(6000L, 0.75f)).rainBrightness(), 1e-6f);
    }

    @Test
    void from_moonPhaseFollowsDayCount() {
        assertEquals(MoonPhase.FULL_MOON, SotoSkyState.from(overworldAt(0L, 0.0f)).moonPhase());
        assertEquals(MoonPhase.WANING_GIBBOUS, SotoSkyState.from(overworldAt(24000L, 0.0f)).moonPhase());
        assertEquals(MoonPhase.FULL_MOON, SotoSkyState.from(overworldAt(8L * 24000L, 0.0f)).moonPhase());
    }

    @Test
    void from_sunriseGlowOnlyNearHorizon() {
        int noon = SotoSkyState.from(overworldAt(6000L, 0.0f)).sunriseSunsetColor();
        int dawn = SotoSkyState.from(overworldAt(23000L, 0.0f)).sunriseSunsetColor();
        assertEquals(0, ARGB.alpha(noon));
        assertTrue(ARGB.alpha(dawn) > 0);
    }

    @Test
    void hasCelestials_overworldOnly() {
        assertTrue(SotoSkyState.from(overworldAt(6000L, 0.0f)).hasCelestials());
        PortalAtmosphere nether = new PortalAtmosphere(
                BuiltinDimensionTypes.NETHER.identifier(), 6000L, 0.0f, 0.0f, 0x330808, 0x330808);
        assertFalse(SotoSkyState.from(SotoAtmosphere.fromPortal(nether)).hasCelestials());
    }
}
