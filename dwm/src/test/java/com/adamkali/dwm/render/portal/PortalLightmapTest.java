package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortalLightmapTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void overlay_overworldDayRaisesSkyFactor() {
        LightmapRenderState state = new LightmapRenderState();
        state.skyFactor = 0.0f;
        SotoAtmosphere atmosphere = new SotoAtmosphere(
                BuiltinDimensionTypes.OVERWORLD.identifier(),
                6000L,
                0.0f,
                0.0f,
                0x78A7FF,
                0xC0D8FF
        );
        PortalLightmap.overlay(state, atmosphere);
        assertTrue(state.needsUpdate);
        assertTrue(state.skyFactor > 0.9f, "day overlay sky factor was " + state.skyFactor);
        assertEquals(0x0A / 255.0f, state.ambientColor.x(), 0.01f);
        assertEquals(0x0A / 255.0f, state.ambientColor.y(), 0.01f);
        assertEquals(0x0A / 255.0f, state.ambientColor.z(), 0.01f);
    }

    @Test
    void overlay_netherKeepsSkyFactorZeroAndSetsAmbient() {
        LightmapRenderState state = new LightmapRenderState();
        state.skyFactor = 1.0f;
        SotoAtmosphere atmosphere = new SotoAtmosphere(
                BuiltinDimensionTypes.NETHER.identifier(),
                6000L,
                0.0f,
                0.0f,
                0x330808,
                0x330808
        );
        PortalLightmap.overlay(state, atmosphere);
        assertEquals(0.0f, state.skyFactor, 1e-4f);
        assertEquals(0x30 / 255.0f, state.ambientColor.x(), 0.01f);
        assertEquals(0x28 / 255.0f, state.ambientColor.y(), 0.01f);
        assertEquals(0x21 / 255.0f, state.ambientColor.z(), 0.01f);
    }
}
