package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;

import static org.junit.jupiter.api.Assertions.*;

class TardisTravelDestinationTest {
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
        model = new TardisDataModel();
        model.uuid = UUID.randomUUID();
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        TardisTravelService.clearActiveForTests();
    }

    @AfterEach
    void tearDown() {
        TardisTravelService.clearActiveForTests();
    }

    @Test
    void hasValidDestinationSelection_biomeRequiresBiomeWhenTagged() {
        model.setDestinationMode(DestinationMode.BIOME);
        model.selectedBiome = null;
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));

        model.selectedBiome = "minecraft:plains";
        assertTrue(TardisTravelService.hasValidDestinationSelection(model));
    }

    @Test
    void hasValidDestinationSelection_waypointRequiresSelectedExisting() {
        model.setDestinationMode(DestinationMode.WAYPOINT);
        model.selectedWaypointId = UUID.randomUUID();
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));

        TardisWaypoint waypoint = WaypointLogic.add(model, "Pad").orElseThrow();
        model.selectedWaypointId = waypoint.id;
        assertTrue(TardisTravelService.hasValidDestinationSelection(model));
    }

    @Test
    void hasValidDestinationSelection_playerRequiresUuid() {
        model.setDestinationMode(DestinationMode.PLAYER);
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));

        model.selectedPlayerUuid = UUID.randomUUID();
        assertTrue(TardisTravelService.hasValidDestinationSelection(model));
    }

    @Test
    void hasValidDestinationSelection_fastReturnRequiresHistoryAndIndex() {
        model.setDestinationMode(DestinationMode.FAST_RETURN);
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));

        FastReturnLogic.pushDeparted(model);
        model.setDestinationMode(DestinationMode.FAST_RETURN);
        model.selectedFastReturnIndex = 0;
        assertTrue(TardisTravelService.hasValidDestinationSelection(model));

        model.selectedFastReturnIndex = 5;
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));
    }

    @Test
    void exactCoordSnapshot_includesFastReturn() {
        model.travelDestinationMode = DestinationMode.FAST_RETURN;
        model.travelDestinationX = 10;
        model.travelDestinationY = 64;
        model.travelDestinationZ = -3;

        Optional<BlockPos> target = TardisTravelService.waypointTargetFromSnapshot(model);

        assertEquals(Optional.of(new BlockPos(10, 64, -3)), target);
        assertTrue(TardisTravelService.isExactCoordMode(DestinationMode.FAST_RETURN));
        assertTrue(TardisTravelService.isExactCoordMode(DestinationMode.WAYPOINT));
        assertFalse(TardisTravelService.isExactCoordMode(DestinationMode.BIOME));
        assertTrue(TardisTravelService.isExactCoordMode(DestinationMode.TELEPATHIC));
    }

    @Test
    void hasValidDestinationSelection_telepathicRequiresPlayerUuid() {
        model.setDestinationMode(DestinationMode.TELEPATHIC);
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));

        model.selectedPlayerUuid = UUID.randomUUID();
        assertTrue(TardisTravelService.hasValidDestinationSelection(model));
    }

    @Test
    void waypointTargetFromSnapshot_readsFlightCoords() {
        model.travelDestinationMode = DestinationMode.WAYPOINT;
        model.travelDestinationX = 100;
        model.travelDestinationY = 70;
        model.travelDestinationZ = -50;

        Optional<BlockPos> target = TardisTravelService.waypointTargetFromSnapshot(model);

        assertEquals(Optional.of(new BlockPos(100, 70, -50)), target);
        assertTrue(TardisTravelService.waypointTargetFromSnapshot(null).isEmpty());

        model.travelDestinationMode = DestinationMode.BIOME;
        assertTrue(TardisTravelService.waypointTargetFromSnapshot(model).isEmpty());
    }

    @Test
    void advanceMaterialisingHold_clearsAllTravelSnapshots() {
        model.setTravelPhase(com.adamkali.dwm.tardis.data.model.TardisTravelPhase.MATERIALISING);
        model.travelPhaseTicks = 1;
        model.travelDestinationBiome = "minecraft:plains";
        model.travelDestinationDimension = "minecraft:overworld";
        model.travelDestinationMode = DestinationMode.WAYPOINT;
        model.travelDestinationX = 1;
        model.travelTargetPlayerUuid = UUID.randomUUID();

        assertTrue(TardisTravelService.advanceMaterialisingHold(model));
        assertNull(model.travelDestinationBiome);
        assertNull(model.travelDestinationDimension);
        assertNull(model.travelDestinationMode);
        assertNull(model.travelTargetPlayerUuid);
        assertEquals(0, model.travelDestinationX);
    }

    @Test
    void effectiveTravelMode_prefersSnapshot() {
        model.setDestinationMode(DestinationMode.PLAYER);
        assertEquals(DestinationMode.PLAYER, TardisTravelService.effectiveTravelMode(model));

        model.travelDestinationMode = DestinationMode.WAYPOINT;
        assertEquals(DestinationMode.WAYPOINT, TardisTravelService.effectiveTravelMode(model));
    }
}
