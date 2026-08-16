package com.adamkali.dwm.network;

import com.adamkali.dwm.gametest.TardisGameTestSupport;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;

public class ChameleonGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void chameleonValidPayloadSmokeFlow(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(TardisChameleonVariant.FIFTH_DOCTOR_BOX.getId(), model.uuid),
                "gametest"
        );
        if (!accepted) {
            throw new AssertionError("Expected valid payload to be accepted");
        }

        boolean rejected = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(Identifier.fromNamespaceAndPath("dwm", "invalid_variant"), model.uuid),
                "gametest"
        );
        if (rejected) {
            throw new AssertionError("Expected invalid payload to be rejected");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void chameleonUpdate_PersistsVariantOnModel(GameTestHelper context) {
        TardisGameTestSupport.configureSaveDirectory(context);
        TardisDataModel model = TardisDataLoader.create();
        TardisChameleonVariant variant = TardisChameleonVariant.SEVENTH_DOCTOR_BOX;

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(variant.getId(), model.uuid),
                "gametest"
        );
        if (!accepted) {
            throw new AssertionError("Expected chameleon update to be accepted");
        }
        if (TardisLogic.getVariant(model.uuid) != variant) {
            throw new AssertionError("Expected persisted variant " + variant + " but was "
                    + TardisLogic.getVariant(model.uuid));
        }
        context.succeed();
    }
}
