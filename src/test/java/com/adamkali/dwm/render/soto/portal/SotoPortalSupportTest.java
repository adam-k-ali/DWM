package com.adamkali.dwm.render.soto.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.GraphicsStatus;

class SotoPortalSupportTest {
    @Test
    void readinessRequiresFastOrFancyStencilTargetAndGhostMeshes() {
        assertTrue(SotoPortalSupport.isReady(true, true, GraphicsStatus.FAST, true, true));
        assertTrue(SotoPortalSupport.isReady(true, true, GraphicsStatus.FANCY, true, true));

        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsStatus.FABULOUS, true, true));
        assertFalse(SotoPortalSupport.isReady(false, true, GraphicsStatus.FAST, true, true));
        assertFalse(SotoPortalSupport.isReady(true, false, GraphicsStatus.FAST, true, true));
        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsStatus.FAST, false, true));
        assertFalse(SotoPortalSupport.isReady(true, true, GraphicsStatus.FAST, true, false));
    }
}
