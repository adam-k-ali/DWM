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
            TardisLogic.toggleDoor(testTardisId);
            assertTrue(testTardis.doorState.isOpen);

            // Test closing the door
            TardisLogic.toggleDoor(testTardisId);
            assertFalse(testTardis.doorState.isOpen);
        }
    }

    @Test
    void toggleDoor_WithNullTardis_ShouldDoNothing() {
        try (MockedStatic<TardisDataLoader> mockedStatic = Mockito.mockStatic(TardisDataLoader.class)) {
            mockedStatic.when(() -> TardisDataLoader.get(testTardisId)).thenReturn(null);
            TardisLogic.toggleDoor(testTardisId);
            // Test passes if no exception is thrown
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
}