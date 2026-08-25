package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import org.jetbrains.annotations.Nullable;

/**
 * Exterior shell alpha during dematerialise / materialise. Elapsed ticks are measured from
 * phase start (countdown start for demat, shell place for mat).
 */
public final class TardisShellOpacity {
    private TardisShellOpacity() {
    }

    public static float alpha(@Nullable TardisTravelPhase phase, float elapsedTicks) {
        if (phase == TardisTravelPhase.MATERIALISING) {
            return clamp01(elapsedTicks / TardisTravelService.MATERIALISING_DURATION_TICKS);
        }
        if (phase == TardisTravelPhase.DEMATERIALISING) {
            return clamp01(1.0f - elapsedTicks / TardisTravelService.DEMATERIALISING_SHELL_REMOVE_AT_TICK);
        }
        return 1.0f;
    }

    private static float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }
}
