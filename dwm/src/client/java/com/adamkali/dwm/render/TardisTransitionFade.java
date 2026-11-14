package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.tardis.logic.TardisTransitionFadeLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

/**
 * Client state for the TARDIS enter/exit transition: while the vanilla loading screen is open for a TARDIS
 * dimension change it is drawn as plain black (no panorama/blur/text), then a short HUD fade-in reveals the world.
 */
@Environment(EnvType.CLIENT)
public final class TardisTransitionFade {
    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_transition_fade");

    private static boolean active;
    private static long fadeStartMs = -1L;

    private TardisTransitionFade() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ELEMENT_ID, TardisTransitionFade::extract);
    }

    /** Called when a respawn packet changes dimension. */
    public static void begin(boolean tardisTransition) {
        active = tardisTransition;
        fadeStartMs = -1L;
    }

    public static boolean isActive() {
        return active;
    }

    /** Called when the loading screen closes; starts the fade-in if this was a TARDIS transition. */
    public static void loadingScreenClosed() {
        if (active) {
            fadeStartMs = Util.getMillis();
            active = false;
        }
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (fadeStartMs < 0L) {
            return;
        }
        float alpha = TardisTransitionFadeLogic.fadeInAlpha(Util.getMillis() - fadeStartMs);
        if (alpha <= 0.0F) {
            fadeStartMs = -1L;
            return;
        }
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), ARGB.color(Math.round(alpha * 255.0F), 0, 0, 0));
    }
}
