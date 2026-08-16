package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoorLockLogicTest {
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        model = new TardisDataModel();
        model.doorState = new TardisDoorState();
    }

    @Test
    void areDoorsClosed_requiresShutAndRestingSwing() {
        assertTrue(DoorLockLogic.areDoorsClosed(model.doorState));

        model.doorState.isOpen = true;
        assertFalse(DoorLockLogic.areDoorsClosed(model.doorState));

        model.doorState.isOpen = false;
        model.doorState.doorSwing = 0.4f;
        assertFalse(DoorLockLogic.areDoorsClosed(model.doorState));
        assertFalse(DoorLockLogic.canToggleLock(model));
        assertFalse(DoorLockLogic.areDoorsClosed(null));
        assertFalse(DoorLockLogic.canToggleLock(null));
    }

    @Test
    void toggle_succeedsWhenFullyClosed() {
        assertFalse(model.doorsLocked);
        assertTrue(DoorLockLogic.toggle(model));
        assertTrue(model.doorsLocked);
        assertFalse(DoorLockLogic.toggle(model));
        assertFalse(model.doorsLocked);
    }

    @Test
    void toggle_refusedWhenOpenOrSwinging() {
        model.doorState.isOpen = true;
        assertFalse(DoorLockLogic.toggle(model));
        assertFalse(model.doorsLocked);

        model.doorState.isOpen = false;
        model.doorState.doorSwing = 0.2f;
        assertFalse(DoorLockLogic.toggle(model));
        assertFalse(model.doorsLocked);
    }

    @Test
    void toggle_doesNotUnlockWhenDoorsAreOpen() {
        assertTrue(DoorLockLogic.toggle(model));
        assertTrue(model.doorsLocked);

        model.doorState.isOpen = true;
        assertTrue(DoorLockLogic.toggle(model));
        assertTrue(model.doorsLocked);
    }
}
