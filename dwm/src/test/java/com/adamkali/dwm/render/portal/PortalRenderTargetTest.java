package com.adamkali.dwm.render.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalRenderTargetTest {
    private final PortalRenderTarget target = PortalRenderTarget.getInstance();

    @AfterEach
    void tearDown() {
        target.close();
    }

    @Test
    void rendersEachPortalKeyAtMostOncePerClientFrame() {
        PortalKey first = PortalKey.boti(UUID.randomUUID());
        PortalKey second = PortalKey.soto(UUID.randomUUID());

        PortalRenderTarget.beginClientFrame();
        assertTrue(target.shouldRenderThisFrame(first));
        assertFalse(target.shouldRenderThisFrame(first));
        assertTrue(target.shouldRenderThisFrame(second));

        PortalRenderTarget.beginClientFrame();
        assertTrue(target.shouldRenderThisFrame(first));
    }

    @Test
    void botiAndSotoKeysAreIndependentPerFrame() {
        UUID id = UUID.randomUUID();
        PortalKey boti = PortalKey.boti(id);
        PortalKey soto = PortalKey.soto(id);

        PortalRenderTarget.beginClientFrame();
        assertTrue(target.shouldRenderThisFrame(boti));
        assertTrue(target.shouldRenderThisFrame(soto));
        assertFalse(target.shouldRenderThisFrame(boti));
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
