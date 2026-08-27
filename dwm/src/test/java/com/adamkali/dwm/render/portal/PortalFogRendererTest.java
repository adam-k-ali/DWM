package com.adamkali.dwm.render.portal;

import net.minecraft.client.renderer.fog.FogData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalFogRendererTest {
    @Test
    void buildFogData_appliesColorAndDistanceToEveryWorldFogRange() {
        FogData fog = PortalFogRenderer.buildFogData(7.0F, 17.0F, 0.2F, 0.3F, 0.4F);

        assertEquals(7.0F, fog.environmentalStart);
        assertEquals(17.0F, fog.environmentalEnd);
        assertEquals(7.0F, fog.renderDistanceStart);
        assertEquals(17.0F, fog.renderDistanceEnd);
        assertEquals(17.0F, fog.skyEnd);
        assertEquals(17.0F, fog.cloudEnd);
        assertEquals(0.2F, fog.color.x);
        assertEquals(0.3F, fog.color.y);
        assertEquals(0.4F, fog.color.z);
        assertEquals(1.0F, fog.color.w);
    }

    @Test
    void buildFogData_rejectsInvalidRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PortalFogRenderer.buildFogData(10.0F, 10.0F, 0.0F, 0.0F, 0.0F)
        );
    }
}
