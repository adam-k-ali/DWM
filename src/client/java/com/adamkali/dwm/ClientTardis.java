package com.adamkali.dwm;

import com.adamkali.dwm.network.UpdateTardisChameleonC2SPayload;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.jetbrains.annotations.NotNull;

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
        TardisLogic.setVariant(this.tardisId, variant);
        ClientPlayNetworking.send(new UpdateTardisChameleonC2SPayload(variant.getId(), this.tardisId));
    }
}
