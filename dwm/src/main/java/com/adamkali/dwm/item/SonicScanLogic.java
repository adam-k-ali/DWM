package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.CloakLogic;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.Reading;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only Scan snapshot for the client HUD.
 */
public final class SonicScanLogic {
    private SonicScanLogic() {
    }

    public record Snapshot(
            boolean noSignal,
            int oxygen,
            int temperature,
            int radiation,
            boolean waterlogged,
            boolean locked,
            boolean cloaked,
            TardisTravelPhase phase,
            int artronPercent,
            boolean artronEmpty
    ) {
    }

    public static Snapshot snapshot(
            @Nullable TardisDataModel model,
            @Nullable Reading reading,
            boolean waterlogged
    ) {
        boolean noSignal = reading == null || reading.noSignal();
        int artron = ArtronLogic.read(model);
        TardisTravelPhase phase = model == null ? TardisTravelPhase.IDLE : model.getTravelPhase();
        return new Snapshot(
                noSignal,
                noSignal ? 0 : percent(reading.oxygen()),
                noSignal ? 0 : percent(reading.temperature()),
                noSignal ? 0 : percent(reading.radiation()),
                waterlogged,
                DoorLockLogic.isLocked(model),
                CloakLogic.isCloaked(model),
                phase,
                ArtronLogic.percent(artron),
                artron <= 0
        );
    }

    private static int percent(float needle) {
        if (ExteriorEnvironmentReadout.isNoSignal(needle)) {
            return 0;
        }
        return Math.round(Math.max(0.0F, Math.min(1.0F, needle)) * 100.0F);
    }
}
