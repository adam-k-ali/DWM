package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

public class ChameleonGameTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chameleonValidPayloadSmokeFlow(TestContext context) {
        TardisDataLoader.tardisSaveDirectory = context.getWorld().getServer().getSavePath(WorldSavePath.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(TardisChameleonVariant.FIFTH_DOCTOR_BOX.getId(), model.uuid),
                "gametest"
        );
        if (!accepted) {
            throw new AssertionError("Expected valid payload to be accepted");
        }

        boolean rejected = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(Identifier.of("dwm", "invalid_variant"), model.uuid),
                "gametest"
        );
        if (rejected) {
            throw new AssertionError("Expected invalid payload to be rejected");
        }

        context.complete();
    }
}
