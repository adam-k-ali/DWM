package com.adamkali.dwm.render.soto.portal;

import net.minecraft.client.option.GraphicsMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SotoPortalSupportTest {
    @Test
    void readinessRequiresFastOrFancyStencilTargetAndGhostMeshes() {
        assertTrue(SotoPortalSupport.isReady(true, true, GraphicsMode.FAST, true, true));
        assertTrue(SotoPortalSupport.isReady(true, true, GraphicsMode.FANCY, true, true));

        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsMode.FABULOUS, true, true));
        assertFalse(SotoPortalSupport.isReady(false, true, GraphicsMode.FAST, true, true));
        assertFalse(SotoPortalSupport.isReady(true, false, GraphicsMode.FAST, true, true));
        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsMode.FAST, false, true));
        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsMode.FAST, true, false));
    }
}
