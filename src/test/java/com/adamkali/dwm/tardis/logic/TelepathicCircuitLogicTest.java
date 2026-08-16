package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TelepathicCircuitLogic.Destination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TelepathicCircuitLogicTest {
    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void resolve_prefersBedOverWorldSpawn() {
        Destination bed = new Destination("minecraft:overworld", 10, 70, -4, true);
        Destination spawn = new Destination("minecraft:overworld", 0, 64, 0, false);

        Destination resolved = TelepathicCircuitLogic.resolve(bed, spawn);

        assertTrue(resolved.usedHome());
        assertEquals(10, resolved.x());
        assertEquals(70, resolved.y());
        assertEquals(-4, resolved.z());
    }

    @Test
    void resolve_fallsBackToWorldSpawnWhenNoBed() {
        Destination spawn = new Destination("minecraft:the_nether", 8, 80, 8, false);

        Destination resolved = TelepathicCircuitLogic.resolve(null, spawn);

        assertFalse(resolved.usedHome());
        assertEquals("minecraft:the_nether", resolved.dimensionId());
        assertEquals(8, resolved.x());
        assertEquals(80, resolved.y());
        assertEquals(8, resolved.z());
    }

    @Test
    void resolve_treatsBlankBedDimensionAsMissing() {
        Destination blank = new Destination("  ", 1, 2, 3, true);
        Destination spawn = new Destination("minecraft:overworld", 0, 64, 0, false);

        Destination resolved = TelepathicCircuitLogic.resolve(blank, spawn);

        assertFalse(resolved.usedHome());
        assertEquals(0, resolved.x());
    }

    @Test
    void arm_setsTelepathicModeAndPlayerUuid() {
        TardisDataModel model = new TardisDataModel();
        UUID player = UUID.randomUUID();
        model.selectedWaypointId = UUID.randomUUID();
        model.selectedFastReturnIndex = 3;

        TelepathicCircuitLogic.arm(model, player);

        assertEquals(DestinationMode.TELEPATHIC, model.getDestinationMode());
        assertEquals(player, model.selectedPlayerUuid);
        assertNull(model.selectedWaypointId);
        assertEquals(0, model.selectedFastReturnIndex);
        assertTrue(TelepathicCircuitLogic.hasSelection(model));
    }

    @Test
    void hasSelection_requiresModeAndPlayer() {
        TardisDataModel model = new TardisDataModel();
        assertFalse(TelepathicCircuitLogic.hasSelection(model));
        model.setDestinationMode(DestinationMode.TELEPATHIC);
        assertFalse(TelepathicCircuitLogic.hasSelection(model));
        model.selectedPlayerUuid = UUID.randomUUID();
        assertTrue(TelepathicCircuitLogic.hasSelection(model));
    }
}
