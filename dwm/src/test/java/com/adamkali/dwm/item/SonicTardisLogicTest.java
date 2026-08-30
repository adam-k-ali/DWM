package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicTardisLogicTest {
    @Test
    void stranger_isNeverRecognised() {
        TardisDataModel model = ownedModel();
        SonicState sonic = SonicState.fullyUnlocked().withSelected(SonicFieldMode.SEAL);
        assertEquals(
                SonicTardisLogic.Decision.NOT_RECOGNISED,
                SonicTardisLogic.decide(model, UUID.randomUUID(), sonic, SonicTardisLogic.Target.EXTERIOR)
        );
    }

    @Test
    void keyedCompanion_doesNotGrantSonicSeal() {
        TardisDataModel model = ownedModel();
        SonicState sonic = SonicState.fullyUnlocked().withSelected(SonicFieldMode.SEAL);
        UUID companion = UUID.randomUUID();
        assertEquals(
                SonicTardisLogic.Decision.NOT_RECOGNISED,
                SonicTardisLogic.decide(model, companion, sonic, SonicTardisLogic.Target.EXTERIOR)
        );
    }

    @Test
    void craftedOwner_handshakesThenSealsWhenSealSelected() {
        TardisDataModel model = ownedModel();
        UUID owner = model.ownerUuid;
        SonicState sonic = SonicState.craftedOpenOnly().withSelected(SonicFieldMode.SEAL);
        // Seal is locked, so selected Seal is unusual; handshake with Open selected is HANDSHAKE_ONLY
        assertEquals(
                SonicTardisLogic.Decision.HANDSHAKE_ONLY,
                SonicTardisLogic.decide(model, owner, SonicState.craftedOpenOnly(), SonicTardisLogic.Target.EXTERIOR)
        );
        SonicState sealSelected = SonicState.craftedOpenOnly()
                .withUnlocked(SonicFieldMode.SEAL)
                .withSelected(SonicFieldMode.SEAL);
        // SEAL unlocked but SCAN/PING still locked → still handshake, then seal
        assertEquals(
                SonicTardisLogic.Decision.HANDSHAKE_THEN_SEAL,
                SonicTardisLogic.decide(model, owner, sealSelected, SonicTardisLogic.Target.EXTERIOR)
        );
    }

    @Test
    void pairedOwner_sealScanOrWrongSettingOnDoors() {
        TardisDataModel model = ownedModel();
        UUID owner = model.ownerUuid;
        SonicState seal = SonicStateLogic.pair(SonicState.craftedOpenOnly()).withSelected(SonicFieldMode.SEAL);
        SonicState scan = SonicStateLogic.pair(SonicState.craftedOpenOnly()).withSelected(SonicFieldMode.SCAN);
        SonicState open = SonicStateLogic.pair(SonicState.craftedOpenOnly());
        assertEquals(
                SonicTardisLogic.Decision.SEAL,
                SonicTardisLogic.decide(model, owner, seal, SonicTardisLogic.Target.EXTERIOR)
        );
        assertEquals(
                SonicTardisLogic.Decision.SCAN,
                SonicTardisLogic.decide(model, owner, scan, SonicTardisLogic.Target.INTERIOR_DOOR)
        );
        assertEquals(
                SonicTardisLogic.Decision.WRONG_SETTING,
                SonicTardisLogic.decide(model, owner, open, SonicTardisLogic.Target.EXTERIOR)
        );
    }

    @Test
    void console_isHandshakeOnlyThenIgnore() {
        TardisDataModel model = ownedModel();
        UUID owner = model.ownerUuid;
        assertEquals(
                SonicTardisLogic.Decision.HANDSHAKE_ONLY,
                SonicTardisLogic.decide(model, owner, SonicState.craftedOpenOnly(), SonicTardisLogic.Target.CONSOLE)
        );
        assertEquals(
                SonicTardisLogic.Decision.IGNORE,
                SonicTardisLogic.decide(
                        model, owner, SonicState.fullyUnlocked(), SonicTardisLogic.Target.CONSOLE)
        );
    }

    @Test
    void isDoor_excludesConsole() {
        assertTrue(SonicTardisLogic.isDoor(SonicTardisLogic.Target.EXTERIOR));
        assertTrue(SonicTardisLogic.isDoor(SonicTardisLogic.Target.INTERIOR_DOOR));
        assertFalse(SonicTardisLogic.isDoor(SonicTardisLogic.Target.CONSOLE));
    }

    private static TardisDataModel ownedModel() {
        TardisDataModel model = new TardisDataModel();
        model.uuid = UUID.randomUUID();
        model.setOwner(UUID.randomUUID());
        return model;
    }
}
