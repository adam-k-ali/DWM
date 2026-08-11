package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.TardisPortalGate;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * Shared BER loop: schedule portal work, peek last texture, composite or placeholder.
 */
public final class PortalDoorRenderer {
    private PortalDoorRenderer() {
    }

    public static boolean shouldRender(float doorSwing) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_DOOR_PORTALS)
                && PortalSupport.isAvailable()
                && TardisPortalGate.shouldShow(doorSwing);
    }

    public static boolean shouldRender(TardisDoorState doorState) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_DOOR_PORTALS)
                && PortalSupport.isAvailable()
                && TardisPortalGate.shouldShow(doorState);
    }

    /**
     * Schedules portal work for END_MAIN and submits deferred doorway composite geometry.
     * Must not touch GL state or open RenderPasses (mid-BER poison on 26.2).
     */
    public static void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            PortalScene scene,
            PortalAperture aperture
    ) {
        if (matrices == null || submitNodeCollector == null || scene == null || aperture == null) {
            return;
        }
        PortalScheduler.schedule(scene);
        PortalRenderer.PortalTexture portalTexture =
                PortalScheduler.peekCompositeTexture(scene.key());
        matrices.pushPose();
        try {
            if (portalTexture.available()) {
                PortalApertureComposite.drawPortalComposite(
                        matrices,
                        submitNodeCollector,
                        aperture,
                        portalTexture
                );
            } else {
                PortalApertureComposite.drawPlaceholder(matrices, submitNodeCollector, aperture);
            }
        } finally {
            matrices.popPose();
        }
    }
}
