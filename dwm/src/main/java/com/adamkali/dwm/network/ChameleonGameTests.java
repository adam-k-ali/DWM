package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;

import java.util.UUID;

public class ChameleonGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void chameleonValidPayloadSmokeFlow(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();
        UUID owner = UUID.randomUUID();
        model.setOwner(owner);

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(TardisChameleonVariant.FIFTH_DOCTOR_BOX.getId(), model.uuid),
                owner,
                null
        );
        if (!accepted) {
            throw new AssertionError("Expected valid payload to be accepted");
        }

        boolean rejected = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(Identifier.fromNamespaceAndPath("dwm", "invalid_variant"), model.uuid),
                owner,
                null
        );
        if (rejected) {
            throw new AssertionError("Expected invalid payload to be rejected");
        }

        boolean visitorRejected = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(
                new UpdateTardisChameleonC2SPayload(TardisChameleonVariant.SIXTH_DOCTOR_BOX.getId(), model.uuid),
                UUID.randomUUID(),
                null
        );
        if (visitorRejected) {
            throw new AssertionError("Expected non-owner chameleon update to be rejected");
        }

        context.succeed();
    }
}
