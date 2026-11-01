package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SotoCloudStateTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static SotoAtmosphere overworldAt(long time, float rain, float thunder) {
        return new SotoAtmosphere(
                BuiltinDimensionTypes.OVERWORLD.identifier(), time, rain, thunder, 0x78A7FF, 0xC0D8FF);
    }

    @Test
    void from_nullFallsBackToDefault() {
        assertEquals(SotoCloudState.from(SotoAtmosphere.DEFAULT), SotoCloudState.from(null));
    }

    @Test
    void from_overworldHasCloudsAtVanillaHeight() {
        SotoCloudState state = SotoCloudState.from(overworldAt(6000L, 0.0f, 0.0f));
        assertTrue(state.hasClouds());
        assertEquals(192.33F, state.height(), 1e-4f);
        assertEquals(ARGB.white(0.8F), state.color());
    }

    @Test
    void from_nonOverworldHasNoClouds() {
        PortalAtmosphere nether = new PortalAtmosphere(
                BuiltinDimensionTypes.NETHER.identifier(), 6000L, 0.0f, 0.0f, 0x330808, 0x330808);
        assertFalse(SotoCloudState.from(SotoAtmosphere.fromPortal(nether)).hasClouds());
    }

    @Test
    void from_cloudsDarkenAtNight() {
        int day = SotoCloudState.from(overworldAt(6000L, 0.0f, 0.0f)).color();
        int night = SotoCloudState.from(overworldAt(18000L, 0.0f, 0.0f)).color();
        assertTrue(ARGB.red(night) < ARGB.red(day));
        assertEquals(ARGB.alpha(day), ARGB.alpha(night));
    }

    @Test
    void from_duskIsBetweenDayAndNight() {
        int day = ARGB.red(SotoCloudState.from(overworldAt(6000L, 0.0f, 0.0f)).color());
        int night = ARGB.red(SotoCloudState.from(overworldAt(18000L, 0.0f, 0.0f)).color());
        int dusk = ARGB.red(SotoCloudState.from(overworldAt(12700L, 0.0f, 0.0f)).color());
        assertTrue(dusk < day && dusk > night);
    }

    @Test
    void from_stormGreysClouds() {
        int clear = SotoCloudState.from(overworldAt(6000L, 0.0f, 0.0f)).color();
        int storm = SotoCloudState.from(overworldAt(6000L, 1.0f, 1.0f)).color();
        assertTrue(ARGB.red(storm) < ARGB.red(clear));
    }

    @Test
    void nightMultiplier_wrapsAcrossMidnightOfDayCount() {
        assertEquals(SotoCloudState.nightMultiplier(6000L), SotoCloudState.nightMultiplier(6000L + 24000L * 5));
        assertEquals(-1, SotoCloudState.nightMultiplier(133L));
    }
}
