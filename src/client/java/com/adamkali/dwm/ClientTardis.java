package com.adamkali.dwm;

import com.adamkali.dwm.network.DeleteWaypointC2SPayload;
import com.adamkali.dwm.network.RenameWaypointC2SPayload;
import com.adamkali.dwm.network.SaveWaypointC2SPayload;
import com.adamkali.dwm.network.SelectPlayerC2SPayload;
import com.adamkali.dwm.network.SelectWaypointC2SPayload;
import com.adamkali.dwm.network.UpdateTardisChameleonC2SPayload;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
        ClientPlayNetworking.send(new UpdateTardisChameleonC2SPayload(variant.getId(), this.tardisId));
    }

    public void saveWaypoint(@Nullable String name) {
        ClientPlayNetworking.send(new SaveWaypointC2SPayload(this.tardisId, name == null ? "" : name));
    }

    public void deleteWaypoint(@NotNull UUID waypointId) {
        ClientPlayNetworking.send(new DeleteWaypointC2SPayload(this.tardisId, waypointId));
    }

    public void renameWaypoint(@NotNull UUID waypointId, @NotNull String name) {
        ClientPlayNetworking.send(new RenameWaypointC2SPayload(this.tardisId, waypointId, name));
    }

    public void selectWaypoint(@Nullable UUID waypointId) {
        ClientPlayNetworking.send(new SelectWaypointC2SPayload(this.tardisId, waypointId));
    }

    public void selectPlayer(@NotNull UUID playerUuid) {
        ClientPlayNetworking.send(new SelectPlayerC2SPayload(this.tardisId, playerUuid));
    }
}
