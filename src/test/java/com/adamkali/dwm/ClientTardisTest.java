package com.adamkali.dwm;

import com.adamkali.dwm.network.UpdateTardisChameleonC2SPayload;
import com.adamkali.dwm.platform.DwmClientPlatform;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.logic.TardisLogic;
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
        DwmClientPlatform platform = Mockito.mock(DwmClientPlatform.class);

        try (MockedStatic<DwmClientServices> services = Mockito.mockStatic(DwmClientServices.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            services.when(DwmClientServices::get).thenReturn(platform);

            clientTardis.updateChameleonVariant(variant);

            Mockito.verify(platform).sendToServer(new UpdateTardisChameleonC2SPayload(variant.getId(), tardisId));
            logic.verifyNoInteractions();
        }
    }
}
