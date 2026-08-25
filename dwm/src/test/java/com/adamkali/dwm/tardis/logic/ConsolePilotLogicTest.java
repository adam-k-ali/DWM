package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsolePilotLogicTest {
    private TardisDataModel model;
    private UUID owner;
    private UUID visitor;

    @BeforeEach
    void setUp() {
        model = new TardisDataModel();
        owner = UUID.randomUUID();
        visitor = UUID.randomUUID();
        model.setOwner(owner);
    }

    @Test
    void isPublicReader_coversAtmosphereAndRefuelerOnly() {
        assertTrue(ConsolePilotLogic.isPublicReader(LookTarget.OXYGEN_READER));
        assertTrue(ConsolePilotLogic.isPublicReader(LookTarget.PRESSURE_READER));
        assertTrue(ConsolePilotLogic.isPublicReader(LookTarget.TEMPERATURE_READER));
        assertTrue(ConsolePilotLogic.isPublicReader(LookTarget.RADIATION_READER));
        assertTrue(ConsolePilotLogic.isPublicReader(LookTarget.REFUELER));

        assertFalse(ConsolePilotLogic.isPublicReader(LookTarget.MATERIALISATION_LEVER));
        assertFalse(ConsolePilotLogic.isPublicReader(LookTarget.CLOAK));
        assertFalse(ConsolePilotLogic.isPublicReader(LookTarget.DOOR_LOCK));
        assertFalse(ConsolePilotLogic.isPublicReader(LookTarget.BIOME_SELECTOR));
        assertFalse(ConsolePilotLogic.isPublicReader(null));
    }

    @Test
    void canPilot_ownerAllowedVisitorAndUnownedRefused() {
        assertTrue(ConsolePilotLogic.canPilot(model, owner));
        assertFalse(ConsolePilotLogic.canPilot(model, visitor));
        assertFalse(ConsolePilotLogic.canPilot(model, null));

        TardisDataModel unowned = new TardisDataModel();
        assertFalse(ConsolePilotLogic.canPilot(unowned, owner));
        assertFalse(ConsolePilotLogic.canPilot(null, owner));
    }

    @Test
    void canInstallCircuit_matchesCanPilot() {
        assertTrue(ConsolePilotLogic.canInstallCircuit(model, owner));
        assertFalse(ConsolePilotLogic.canInstallCircuit(model, visitor));
        assertFalse(ConsolePilotLogic.canInstallCircuit(new TardisDataModel(), owner));
    }

    @Test
    void canToggleDoorLock_ownerAlwaysAllowed() {
        assertTrue(ConsolePilotLogic.canToggleDoorLock(model, owner, null));
        assertTrue(ConsolePilotLogic.canToggleDoorLock(model, owner, UUID.randomUUID()));
    }

    @Test
    void canToggleDoorLock_visitorNeedsMatchingBoundKey() {
        assertFalse(ConsolePilotLogic.canToggleDoorLock(model, visitor, null));
        assertFalse(ConsolePilotLogic.canToggleDoorLock(model, visitor, UUID.randomUUID()));
        assertTrue(ConsolePilotLogic.canToggleDoorLock(model, visitor, model.uuid));
    }

    @Test
    void canToggleDoorLock_unownedRefusesEvenWithMatchingKey() {
        TardisDataModel unowned = new TardisDataModel();
        // Key still TOGGLE_READY for matching UUID; ownership is not required for key toggle.
        assertTrue(ConsolePilotLogic.canToggleDoorLock(unowned, visitor, unowned.uuid));
        assertFalse(ConsolePilotLogic.canToggleDoorLock(unowned, visitor, null));
    }
}
