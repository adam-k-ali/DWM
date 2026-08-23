package com.adamkali.dwm;

import com.adamkali.dwm.network.DeleteWaypointC2SPayload;
import com.adamkali.dwm.network.RenameWaypointC2SPayload;
import com.adamkali.dwm.network.SaveWaypointC2SPayload;
import com.adamkali.dwm.network.SelectPlayerC2SPayload;
import com.adamkali.dwm.network.SelectWaypointC2SPayload;
import com.adamkali.dwm.network.UpdateTardisChameleonC2SPayload;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClientTardis {
    private final UUID tardisId;

    public ClientTardis(UUID tardisId) {
        this.tardisId = tardisId;
    }

    public UUID getTardisId() {
        return tardisId;
    }

    public void updateChameleonVariant(@NotNull TardisChameleonVariant variant) {
        DwmClientServices.get().sendToServer(new UpdateTardisChameleonC2SPayload(variant.getId(), this.tardisId));
    }

    public void saveWaypoint(@Nullable String name) {
        DwmClientServices.get().sendToServer(new SaveWaypointC2SPayload(this.tardisId, name == null ? "" : name));
    }

    public void deleteWaypoint(@NotNull UUID waypointId) {
        DwmClientServices.get().sendToServer(new DeleteWaypointC2SPayload(this.tardisId, waypointId));
    }

    public void renameWaypoint(@NotNull UUID waypointId, @NotNull String name) {
        DwmClientServices.get().sendToServer(new RenameWaypointC2SPayload(this.tardisId, waypointId, name));
    }

    public void selectWaypoint(@Nullable UUID waypointId) {
        DwmClientServices.get().sendToServer(new SelectWaypointC2SPayload(this.tardisId, waypointId));
    }

    public void selectPlayer(@Nullable UUID playerUuid) {
        DwmClientServices.get().sendToServer(new SelectPlayerC2SPayload(this.tardisId, playerUuid));
    }
}
