package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import net.minecraft.util.ActionResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TardisTravelServiceTest {
    private UUID tardisId;
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        tardisId = UUID.randomUUID();
        model = new TardisDataModel();
        model.uuid = tardisId;
        TardisTravelService.clearActiveForTests();
    }

    @AfterEach
    void tearDown() {
        TardisTravelService.clearActiveForTests();
    }

    @Test
    void startTravel_failsWithoutExteriorOrBiome() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);

            assertEquals(ActionResult.FAIL, TardisTravelService.startTravel(tardisId, null));

            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            assertEquals(ActionResult.FAIL, TardisTravelService.startTravel(tardisId, null));

            model.selectedBiome = "minecraft:plains";
            // server null still fails
            assertEquals(ActionResult.FAIL, TardisTravelService.startTravel(tardisId, null));
        }
    }

    @Test
    void startTravel_returnsPassWhenAlreadyTraveling() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            model.selectedBiome = "minecraft:plains";
            model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);

            assertEquals(ActionResult.PASS, TardisTravelService.startTravel(tardisId, null));
            assertTrue(TardisTravelService.isTraveling(tardisId));
        }
    }

    @Test
    void isTraveling_falseWhenIdleOrMissing() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            assertFalse(TardisTravelService.isTraveling(tardisId));
            assertFalse(TardisTravelService.isTraveling(null));
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(null);
            assertFalse(TardisTravelService.isTraveling(tardisId));
        }
    }
}
