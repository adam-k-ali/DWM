package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Pure rules for the smooth enter/exit transition: which dimension changes get the DWM fade instead of
 * vanilla's "Downloading terrain" screen, and how opaque the fade-in overlay is after the level is ready.
 */
public final class TardisTransitionFadeLogic {
    public static final long FADE_IN_MS = 350L;

    private TardisTransitionFadeLogic() {
    }

    /** True when a respawn changes dimension and either side is the TARDIS interior dimension. */
    public static boolean isTardisTransition(ResourceKey<Level> from, ResourceKey<Level> to) {
        if (from == null || to == null || from.equals(to)) {
            return false;
        }
        return TardisDimensions.TARDIS_WORLD_KEY.equals(from) || TardisDimensions.TARDIS_WORLD_KEY.equals(to);
    }

    /** Overlay opacity in [0,1] {@code elapsedMs} after the loading screen closed (1 = black, 0 = clear). */
    public static float fadeInAlpha(long elapsedMs) {
        if (elapsedMs <= 0L) {
            return 1.0F;
        }
        if (elapsedMs >= FADE_IN_MS) {
            return 0.0F;
        }
        float t = (float) elapsedMs / (float) FADE_IN_MS;
        return 1.0F - t * t * (3.0F - 2.0F * t);
    }
}
