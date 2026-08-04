package com.adamkali.dwm.render.soto.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SotoPortalRenderTargetTest {
    private final SotoPortalRenderTarget target = SotoPortalRenderTarget.getInstance();

    @AfterEach
    void tearDown() {
        target.close();
    }

    @Test
    void rendersEachTardisAtMostOncePerClientFrame() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        SotoPortalRenderTarget.beginClientFrame();
        assertTrue(target.shouldRenderThisFrame(first));
        assertFalse(target.shouldRenderThisFrame(first));
        assertTrue(target.shouldRenderThisFrame(second));

        SotoPortalRenderTarget.beginClientFrame();
        assertTrue(target.shouldRenderThisFrame(first));
    }

    @Test
    void closeInvalidatesFramebufferMetadata() {
        target.close();
        target.close();

        assertFalse(target.isReady());
        assertEquals(-1, target.colorTextureId());
        assertEquals(0, target.width());
        assertEquals(0, target.height());
    }
}
