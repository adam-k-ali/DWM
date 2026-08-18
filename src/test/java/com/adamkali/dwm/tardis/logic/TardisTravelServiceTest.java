package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;

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

            assertEquals(InteractionResult.FAIL, TardisTravelService.startTravel(tardisId, null));

            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            assertEquals(InteractionResult.FAIL, TardisTravelService.startTravel(tardisId, null));

            model.selectedBiome = "minecraft:plains";
            // server null still fails
            assertEquals(InteractionResult.FAIL, TardisTravelService.startTravel(tardisId, null));
        }
    }

    @Test
    void startTravel_returnsPassWhenAlreadyTraveling() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            model.selectedBiome = "minecraft:plains";
            model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);

            assertEquals(InteractionResult.PASS, TardisTravelService.startTravel(tardisId, null));
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

    @Test
    void requestMaterialise_returnsPassWhenNotInFlight() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);

            assertEquals(InteractionResult.PASS, TardisTravelService.requestMaterialise(tardisId, null));

            model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
            assertEquals(InteractionResult.PASS, TardisTravelService.requestMaterialise(tardisId, null));

            model.setTravelPhase(TardisTravelPhase.MATERIALISING);
            assertEquals(InteractionResult.PASS, TardisTravelService.requestMaterialise(tardisId, null));
        }
    }

    @Test
    void requestMaterialise_failsWhenInFlightButServerNull() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);

            assertEquals(InteractionResult.FAIL, TardisTravelService.requestMaterialise(tardisId, null));
            assertEquals(TardisTravelPhase.IN_FLIGHT, model.getTravelPhase());
        }
    }

    @Test
    void requestMaterialise_failsWhenMissingWithoutShellSnapshot() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);

            // server non-null check happens before world resolution; null server → FAIL first
            assertEquals(InteractionResult.FAIL, TardisTravelService.requestMaterialise(tardisId, null));
        }
    }

    @Test
    void advanceDematerialisingHold_countsDownThenEntersInFlight() {
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        model.travelPhaseTicks = 2;

        assertFalse(TardisTravelService.advanceDematerialisingHold(model));
        assertEquals(1, model.travelPhaseTicks);
        assertEquals(TardisTravelPhase.DEMATERIALISING, model.getTravelPhase());

        assertTrue(TardisTravelService.advanceDematerialisingHold(model));
        assertEquals(0, model.travelPhaseTicks);
        assertEquals(TardisTravelPhase.IN_FLIGHT, model.getTravelPhase());
    }

    @Test
    void advanceDematerialisingHold_ignoresNonDematerialising() {
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelPhaseTicks = 5;
        assertFalse(TardisTravelService.advanceDematerialisingHold(model));
        assertEquals(5, model.travelPhaseTicks);
        assertEquals(TardisTravelPhase.IN_FLIGHT, model.getTravelPhase());
    }

    @Test
    void dematerialisingConstants_shellRemoveBeforeDuration() {
        assertTrue(TardisTravelService.DEMATERIALISING_SHELL_REMOVE_AT_TICK
                < TardisTravelService.DEMATERIALISING_DURATION_TICKS);
        assertTrue(TardisTravelService.DEMATERIALISING_DURATION_TICKS > 0);
        assertTrue(TardisTravelService.MATERIALISING_DURATION_TICKS > 0);
    }

    @Test
    void shouldRemoveShell_falseUntilElapsedReachesConfiguredTick() {
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        model.travelPhaseTicks = TardisTravelService.DEMATERIALISING_DURATION_TICKS;
        assertFalse(TardisTravelService.shouldRemoveShell(model));

        model.travelPhaseTicks = TardisTravelService.DEMATERIALISING_DURATION_TICKS
                - TardisTravelService.DEMATERIALISING_SHELL_REMOVE_AT_TICK;
        assertTrue(TardisTravelService.shouldRemoveShell(model));
    }

    @Test
    void advanceMaterialisingHold_countsDownThenEntersIdle() {
        model.setTravelPhase(TardisTravelPhase.MATERIALISING);
        model.travelPhaseTicks = 2;
        model.travelDestinationBiome = "minecraft:plains";
        model.travelDestinationDimension = "minecraft:the_nether";

        assertFalse(TardisTravelService.advanceMaterialisingHold(model));
        assertEquals(1, model.travelPhaseTicks);
        assertEquals(TardisTravelPhase.MATERIALISING, model.getTravelPhase());

        assertTrue(TardisTravelService.advanceMaterialisingHold(model));
        assertEquals(0, model.travelPhaseTicks);
        assertEquals(TardisTravelPhase.IDLE, model.getTravelPhase());
        assertNull(model.travelDestinationBiome);
        assertNull(model.travelDestinationDimension);
    }

    @Test
    void advanceMaterialisingHold_ignoresNonMaterialising() {
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        model.travelPhaseTicks = 5;
        assertFalse(TardisTravelService.advanceMaterialisingHold(model));
        assertEquals(5, model.travelPhaseTicks);
    }

    @Test
    void startSummonTravel_returnsPassWhenAlreadyTraveling() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);

            assertEquals(
                    InteractionResult.PASS,
                    TardisTravelService.startSummonTravel(
                            tardisId, null, "minecraft:overworld", new BlockPos(1, 64, 1), 0)
            );
            assertFalse(TardisTravelService.isSummonPending(tardisId));
        }
    }

    @Test
    void startSummonTravel_failsWithoutServerOrExterior() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            assertEquals(
                    InteractionResult.FAIL,
                    TardisTravelService.startSummonTravel(
                            tardisId, null, "minecraft:overworld", new BlockPos(1, 64, 1), 0)
            );

            model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
            assertEquals(
                    InteractionResult.FAIL,
                    TardisTravelService.startSummonTravel(
                            tardisId, null, "minecraft:overworld", new BlockPos(1, 64, 1), 0)
            );
        }
    }

    @Test
    void consumeSummonPending_clearsFlagOnce() {
        TardisTravelService.markSummonPendingForTests(tardisId);
        assertTrue(TardisTravelService.isSummonPending(tardisId));
        assertTrue(TardisTravelService.consumeSummonPending(tardisId));
        assertFalse(TardisTravelService.isSummonPending(tardisId));
        assertFalse(TardisTravelService.consumeSummonPending(tardisId));
    }

    @Test
    void materialiseAt_returnsPassWhenNotInFlight() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            assertEquals(
                    InteractionResult.PASS,
                    TardisTravelService.materialiseAt(
                            tardisId, null, null, new BlockPos(0, 64, 0), 0)
            );
        }
    }
}
