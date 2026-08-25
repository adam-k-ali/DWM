package com.adamkali.dwm.render.portal;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.BlendFactor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalDoorRendererTest {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("dwm", "textures/entity/first_doctor_box.png");

    @Test
    void doorOverlay_usesTranslucentPass() {
        assertEquals(
                RenderTypes.entityTranslucent(TEXTURE),
                PortalDoorRenderer.doorOverlayRenderType(TEXTURE)
        );
    }

    @Test
    void doorOverlay_isNotShellCutoutBucket() {
        assertNotEquals(
                RenderTypes.entityCutout(TEXTURE),
                PortalDoorRenderer.doorOverlayRenderType(TEXTURE)
        );
    }

    @Test
    void doorOverlay_isNotPortalCutoutBucket() {
        assertNotEquals(
                RenderTypes.entityCutout(PortalSamplingTexture.ID),
                PortalDoorRenderer.doorOverlayRenderType(TEXTURE)
        );
    }

    @Test
    void portalComposite_isNotEmissiveTranslucent() {
        assertNotEquals(
                RenderTypes.entityTranslucentEmissive(PortalSamplingTexture.ID),
                PortalDoorRenderer.portalCompositeRenderType()
        );
    }

    @Test
    void portalComposite_isNotEntityCutoutBucket() {
        assertNotEquals(
                RenderTypes.entityCutout(PortalSamplingTexture.ID),
                PortalDoorRenderer.portalCompositeRenderType()
        );
    }

    @Test
    void portalComposite_doesNotWriteDepth() {
        var depth = PortalDoorRenderer.portalCompositeRenderType().pipeline().getDepthStencilState();
        assertNotNull(depth);
        assertFalse(depth.writeDepth());
    }

    @Test
    void portalComposite_usesOverwriteBlendForLatePass() {
        var type = PortalDoorRenderer.portalCompositeRenderType();
        assertTrue(type.hasBlending(), "blending flag routes submitCustomGeometry after the shell");
        var blend = type.pipeline().getColorTargetState().blendFunction().orElseThrow();
        assertEquals(BlendFactor.ONE, blend.color().sourceFactor());
        assertEquals(BlendFactor.ZERO, blend.color().destFactor());
    }

    @Test
    void doorOverlay_stillWritesDepth() {
        var depth = PortalDoorRenderer.doorOverlayRenderType(TEXTURE).pipeline().getDepthStencilState();
        assertNotNull(depth);
        assertTrue(depth.writeDepth());
    }
}
