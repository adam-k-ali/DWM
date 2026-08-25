package com.adamkali.dwm.render.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.GraphicsPreset;

class PortalSupportTest {
    @Test
    void readinessRequiresSupportedPresetAndTarget() {
        assertTrue(PortalSupport.isReady(true, GraphicsPreset.FAST, true));
        assertTrue(PortalSupport.isReady(true, GraphicsPreset.FANCY, true));
        assertTrue(PortalSupport.isReady(true, GraphicsPreset.CUSTOM, true));

        assertFalse(PortalSupport.isReady(true, GraphicsPreset.FABULOUS, true));
        assertFalse(PortalSupport.isReady(false, GraphicsPreset.FAST, true));
        assertFalse(PortalSupport.isReady(true, GraphicsPreset.FAST, false));
    }

    @Test
    void supportedPresetsAllowCustomButNotFabulous() {
        assertTrue(PortalSupport.isSupportedGraphicsPreset(GraphicsPreset.FAST));
        assertTrue(PortalSupport.isSupportedGraphicsPreset(GraphicsPreset.FANCY));
        assertTrue(PortalSupport.isSupportedGraphicsPreset(GraphicsPreset.CUSTOM));
        assertFalse(PortalSupport.isSupportedGraphicsPreset(GraphicsPreset.FABULOUS));
        assertFalse(PortalSupport.isSupportedGraphicsPreset(null));
    }
}
