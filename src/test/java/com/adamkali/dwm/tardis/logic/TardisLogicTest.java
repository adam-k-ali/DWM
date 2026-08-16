package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;
import net.minecraft.world.InteractionResult;

import static org.junit.jupiter.api.Assertions.*;

class TardisLogicTest {
    private UUID testTardisId;
    private TardisDataModel testTardis;
    private TardisDoorState testDoorState;

    @BeforeEach
    void setUp() {
        testTardisId = UUID.randomUUID();
        testTardis = new TardisDataModel();
        testDoorState = new TardisDoorState();
        testTardis.doorState = testDoorState;
        testTardis.variant = TardisChameleonVariant.FIFTH_DOCTOR_BOX;
    }

    @Test
    void toggleDoor_ShouldToggleDoorState() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            // Test opening the door
            assertFalse(testTardis.doorState.isOpen);
            InteractionResult firstResult = TardisLogic.toggleDoor(testTardisId);
            assertEquals(InteractionResult.SUCCESS, firstResult);
            assertTrue(testTardis.doorState.isOpen);

            // Test closing the door
            InteractionResult secondResult = TardisLogic.toggleDoor(testTardisId);
            assertEquals(InteractionResult.SUCCESS, secondResult);
            assertFalse(testTardis.doorState.isOpen);
        }
    }

    @Test
    void toggleDoor_WhenDoorSwingInTransition_ShouldReturnPassWithoutStateMutation() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);
            testTardis.doorState.isOpen = false;
            testTardis.doorState.doorSwing = 0.4f;

            InteractionResult result = TardisLogic.toggleDoor(testTardisId);

            assertEquals(InteractionResult.PASS, result);
            assertFalse(testTardis.doorState.isOpen);
            assertEquals(0.4f, testTardis.doorState.doorSwing, 0.001f);
        }
    }

    @Test
    void toggleDoor_WithNullTardis_ShouldDoNothing() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(null);
            InteractionResult result = TardisLogic.toggleDoor(testTardisId);
            assertEquals(InteractionResult.FAIL, result);
        }
    }

    @Test
    void getDoorState_ShouldReturnCorrectState() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            TardisDoorState returnedState = TardisLogic.getDoorState(testTardisId);
            assertNotNull(returnedState);
            assertEquals(testDoorState, returnedState);
        }
    }

    @Test
    void getDoorState_WithNullTardis_ShouldReturnNull() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(null);

            TardisDoorState returnedState = TardisLogic.getDoorState(testTardisId);
            assertNull(returnedState);
        }
    }

    @Test
    void updateDoorState_WhenDoorOpening_ShouldIncreaseDoorSwing() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            testTardis.doorState.isOpen = true;
            testTardis.doorState.doorSwing = 0.5f;

            TardisLogic.updateDoorState(testTardisId);

            assertEquals(0.55f, testTardis.doorState.doorSwing, 0.001f);
        }
    }

    @Test
    void updateDoorState_WhenDoorOpening_ShouldClampToOne() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            testTardis.doorState.isOpen = true;
            testTardis.doorState.doorSwing = 0.99f;

            TardisLogic.updateDoorState(testTardisId);

            assertEquals(1.0f, testTardis.doorState.doorSwing, 0.001f);
        }
    }

    @Test
    void updateDoorState_WhenDoorClosing_ShouldDecreaseDoorSwing() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            testTardis.doorState.isOpen = false;
            testTardis.doorState.doorSwing = 0.5f;

            TardisLogic.updateDoorState(testTardisId);

            assertEquals(0.45f, testTardis.doorState.doorSwing, 0.001f);
        }
    }

    @Test
    void updateDoorState_WhenDoorClosing_ShouldClampToZero() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            testTardis.doorState.isOpen = false;
            testTardis.doorState.doorSwing = 0.01f;

            TardisLogic.updateDoorState(testTardisId);

            assertEquals(0.0f, testTardis.doorState.doorSwing, 0.001f);
        }
    }

    @Test
    void setVariant_ShouldUpdateVariant() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            TardisLogic.setVariant(testTardisId, TardisChameleonVariant.FOURTH_DOCTOR_BOX);
            assertEquals(TardisChameleonVariant.FOURTH_DOCTOR_BOX, testTardis.variant);
        }
    }

    @Test
    void getVariant_ShouldReturnCorrectVariant() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);

            TardisChameleonVariant returnedVariant = TardisLogic.getVariant(testTardisId);
            assertEquals(TardisChameleonVariant.FIFTH_DOCTOR_BOX, returnedVariant);
        }
    }

    @Test
    void getVariant_WithNullTardis_ShouldReturnNull() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(null);

            TardisChameleonVariant returnedVariant = TardisLogic.getVariant(testTardisId);
            assertNull(returnedVariant);
        }
    }

    @Test
    void getTravelPhase_defaultsToIdleWhenMissing() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(null);
            assertEquals(
                    com.adamkali.dwm.tardis.data.model.TardisTravelPhase.IDLE,
                    TardisLogic.getTravelPhase(testTardisId)
            );
            assertEquals(
                    com.adamkali.dwm.tardis.data.model.TardisTravelPhase.IDLE,
                    TardisLogic.getTravelPhase(null)
            );
        }
    }

    @Test
    void cloakFlag_persistsOnModelAndEquals() {
        testTardis.uuid = testTardisId;
        testTardis.cloaked = true;
        TardisDataModel copy = new TardisDataModel();
        copy.uuid = testTardisId;
        copy.doorState = testTardis.doorState;
        copy.variant = testTardis.variant;
        copy.cloaked = true;
        assertEquals(testTardis, copy);
        assertTrue(CloakLogic.isCloaked(testTardis));
        assertFalse(CloakLogic.toggle(testTardis));
        assertFalse(testTardis.cloaked);
    }

    @Test
    void getTravelPhase_returnsModelPhase() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(testTardis);
            testTardis.setTravelPhase(com.adamkali.dwm.tardis.data.model.TardisTravelPhase.IN_FLIGHT);
            assertEquals(
                    com.adamkali.dwm.tardis.data.model.TardisTravelPhase.IN_FLIGHT,
                    TardisLogic.getTravelPhase(testTardisId)
            );
        }
    }
}