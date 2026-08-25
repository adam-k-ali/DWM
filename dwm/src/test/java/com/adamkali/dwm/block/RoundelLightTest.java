package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundelLightTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void roundels_emitLightLevel10() {
        assertEquals(10, DWMBlocks.WHITE_ROUNDEL_A.defaultBlockState().getLightEmission());
        assertEquals(10, DWMBlocks.WHITE_ROUNDEL_B.defaultBlockState().getLightEmission());
        assertEquals(10, DWMBlocks.WHITE_BIG_ROUNDEL_A.defaultBlockState().getLightEmission());
        assertEquals(10, DWMBlocks.WHITE_BIG_ROUNDEL_B.defaultBlockState().getLightEmission());
        assertEquals(10, DWMBlocks.TEAL_ROUNDEL_A.defaultBlockState().getLightEmission());
        assertEquals(10, DWMBlocks.BLACK_BIG_ROUNDEL_B.defaultBlockState().getLightEmission());
    }

    @Test
    void tardisWalls_doNotEmitLight() {
        assertEquals(0, DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.LIGHT_GRAY_TARDIS_WALL.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.TEAL_TARDIS_WALL.defaultBlockState().getLightEmission());
    }
}
