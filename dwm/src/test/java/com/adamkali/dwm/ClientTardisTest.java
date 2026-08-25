package com.adamkali.dwm;

import com.adamkali.dwm.network.UpdateTardisChameleonC2SPayload;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

class ClientTardisTest {
    @Test
    void updateChameleonVariant_sendsPayloadToServer() {
        UUID tardisId = UUID.randomUUID();
        ClientTardis clientTardis = new ClientTardis(tardisId);
        TardisChameleonVariant variant = TardisChameleonVariant.FOURTH_DOCTOR_BOX;

        try (MockedStatic<ClientPlayNetworking> networking = Mockito.mockStatic(ClientPlayNetworking.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            clientTardis.updateChameleonVariant(variant);

            networking.verify(() -> ClientPlayNetworking.send(
                    new UpdateTardisChameleonC2SPayload(variant.getId(), tardisId)
            ));
            logic.verifyNoInteractions();
        }
    }
}
