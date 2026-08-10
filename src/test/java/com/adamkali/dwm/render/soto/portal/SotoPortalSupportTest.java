package com.adamkali.dwm.render.soto.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.GraphicsPreset;

class SotoPortalSupportTest {
    @Test
    void readinessRequiresFastOrFancyStencilTargetAndGhostMeshes() {
        assertTrue(SotoPortalSupport.isReady(true, true, GraphicsPreset.FAST, true, true));
        assertTrue(SotoPortalSupport.isReady(true, true, GraphicsPreset.FANCY, true, true));

        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsPreset.FABULOUS, true, true));
        assertFalse(SotoPortalSupport.isReady(false, true, GraphicsPreset.FAST, true, true));
        assertFalse(SotoPortalSupport.isReady(true, false, GraphicsPreset.FAST, true, true));
        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsPreset.FAST, false, true));
        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsPreset.FAST, true, false));
    }
}
