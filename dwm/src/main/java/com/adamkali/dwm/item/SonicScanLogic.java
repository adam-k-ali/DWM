package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.CloakLogic;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.Reading;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only Scan overlay. Prefix {@code Scan:} is substring-stable for Screenplay.
 */
public final class SonicScanLogic {
    public static final String PREFIX = "Scan: ";

    private SonicScanLogic() {
    }

    public static Component overlay(
            @Nullable TardisDataModel model,
            @Nullable Reading reading,
            boolean waterlogged
    ) {
        return Component.literal(PREFIX + body(model, reading, waterlogged));
    }

    public static String body(
            @Nullable TardisDataModel model,
            @Nullable Reading reading,
            boolean waterlogged
    ) {
        StringBuilder body = new StringBuilder();
        if (reading == null || reading.noSignal()) {
            body.append("No exterior signal");
        } else {
            body.append("Oxygen: ").append(percent(reading.oxygen())).append('%');
            body.append("; Waterlogged: ").append(waterlogged ? "yes" : "no");
            body.append("; Temperature: ").append(percent(reading.temperature())).append('%');
            body.append("; Radiation: ").append(percent(reading.radiation())).append('%');
        }
        body.append("; Locked: ").append(DoorLockLogic.isLocked(model) ? "yes" : "no");
        body.append("; Cloaked: ").append(CloakLogic.isCloaked(model) ? "yes" : "no");
        TardisTravelPhase phase = model == null ? TardisTravelPhase.IDLE : model.getTravelPhase();
        body.append("; Phase: ").append(phase.name());
        body.append("; ").append(plainArtron(model));
        return body.toString();
    }

    private static String plainArtron(@Nullable TardisDataModel model) {
        int artron = ArtronLogic.read(model);
        if (artron <= 0) {
            return "Artron reserves: empty";
        }
        return "Artron reserves: " + ArtronLogic.percent(artron) + "%";
    }

    private static int percent(float needle) {
        if (ExteriorEnvironmentReadout.isNoSignal(needle)) {
            return 0;
        }
        return Math.round(Math.max(0.0F, Math.min(1.0F, needle)) * 100.0F);
    }
}
