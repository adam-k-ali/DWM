package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.CircuitFittedLogic;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonicPingLogicTest {
    @Test
    void lockedPing_isNotInstalled() {
        assertEquals(
                SonicPingLogic.Result.NOT_INSTALLED,
                SonicPingLogic.evaluate(false, cloakedNearby(), "minecraft:overworld", 0, 64, 0, 0L, Long.MIN_VALUE)
        );
    }

    @Test
    void missingOwnedTardis_isNoSignal() {
        assertEquals(
                SonicPingLogic.Result.NO_SIGNAL,
                SonicPingLogic.evaluate(true, null, "minecraft:overworld", 0, 64, 0, 0L, Long.MIN_VALUE)
        );
    }

    @Test
    void cloakNotFitted_beatsRange() {
        TardisDataModel model = cloakedNearby();
        CircuitFittedLogic.setFitted(model, TardisCircuit.CLOAK, false);
        assertEquals(
                SonicPingLogic.Result.CLOAK_NOT_FITTED,
                SonicPingLogic.evaluate(true, model, "minecraft:overworld", 0, 64, 0, 0L, Long.MIN_VALUE)
        );
    }

    @Test
    void cloakOff_isNotEngagedEvenWhenInRange() {
        TardisDataModel model = cloakedNearby();
        model.cloaked = false;
        assertEquals(
                SonicPingLogic.Result.CLOAK_NOT_ENGAGED,
                SonicPingLogic.evaluate(true, model, "minecraft:overworld", 0, 64, 0, 0L, Long.MIN_VALUE)
        );
    }

    @Test
    void outOfRange_isNoSignal() {
        TardisDataModel model = cloakedNearby();
        assertEquals(
                SonicPingLogic.Result.NO_SIGNAL,
                SonicPingLogic.evaluate(true, model, "minecraft:overworld", 1000, 64, 1000, 0L, Long.MIN_VALUE)
        );
    }

    @Test
    void otherDimension_isNoSignal() {
        TardisDataModel model = cloakedNearby();
        assertEquals(
                SonicPingLogic.Result.NO_SIGNAL,
                SonicPingLogic.evaluate(true, model, "minecraft:the_nether", 0, 64, 0, 0L, Long.MIN_VALUE)
        );
    }

    @Test
    void success_thenCooldown() {
        TardisDataModel model = cloakedNearby();
        assertEquals(
                SonicPingLogic.Result.LOCATED,
                SonicPingLogic.evaluate(true, model, "minecraft:overworld", 0, 64, 0, 100L, Long.MIN_VALUE)
        );
        assertEquals(
                SonicPingLogic.Result.ON_COOLDOWN,
                SonicPingLogic.evaluate(true, model, "minecraft:overworld", 0, 64, 0, 120L, 100L)
        );
        assertEquals(
                SonicPingLogic.Result.LOCATED,
                SonicPingLogic.evaluate(true, model, "minecraft:overworld", 0, 64, 0, 140L, 100L)
        );
    }

    private static TardisDataModel cloakedNearby() {
        TardisDataModel model = new TardisDataModel();
        model.hasExteriorLocation = true;
        model.exteriorDimension = "minecraft:overworld";
        model.exteriorX = 0;
        model.exteriorY = 64;
        model.exteriorZ = 0;
        model.cloaked = true;
        model.cloakFitted = true;
        return model;
    }
}
