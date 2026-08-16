package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TardisLogicDestinationModeTest {
    private UUID tardisId;
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
        tardisId = UUID.randomUUID();
        model = new TardisDataModel();
        model.uuid = tardisId;
        model.variant = TardisChameleonVariant.TT_CAPSULE;
        model.setExteriorLocation("minecraft:overworld", 0, 70, 0, 0);
    }

    @Test
    void cycleVariant_wrapsFromLastToFirst() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<FirstDoctorConsoleSync> sync = Mockito.mockStatic(FirstDoctorConsoleSync.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            TardisChameleonVariant[] values = TardisChameleonVariant.values();
            model.variant = values[values.length - 1];

            Optional<TardisChameleonVariant> next = TardisLogic.cycleVariant(tardisId, null);

            assertTrue(next.isPresent());
            assertEquals(values[0], next.get());
            assertEquals(values[0], model.variant);
            sync.verify(() -> FirstDoctorConsoleSync.syncFromModel(null, tardisId));
        }
    }

    @Test
    void cycleVariant_advancesOrdinal() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<FirstDoctorConsoleSync> sync = Mockito.mockStatic(FirstDoctorConsoleSync.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            model.variant = TardisChameleonVariant.TT_CAPSULE;

            Optional<TardisChameleonVariant> next = TardisLogic.cycleVariant(tardisId, null);

            assertEquals(Optional.of(TardisChameleonVariant.FIRST_DOCTOR_BOX), next);
            assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, model.variant);
        }
    }

    @Test
    void selectWaypoint_and_selectPlayer_switchModes() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            TardisWaypoint waypoint = WaypointLogic.add(model, "Pad").orElseThrow();

            assertTrue(TardisLogic.selectWaypoint(tardisId, waypoint.id));
            assertEquals(DestinationMode.WAYPOINT, model.getDestinationMode());

            UUID playerId = UUID.randomUUID();
            assertTrue(TardisLogic.selectPlayer(tardisId, playerId, null));
            assertEquals(DestinationMode.PLAYER, model.getDestinationMode());
            assertEquals(playerId, model.selectedPlayerUuid);
            assertNull(model.selectedWaypointId);
        }
    }

    @Test
    void clearNonBiomeSelection_resetsMode() {
        model.setDestinationMode(DestinationMode.PLAYER);
        model.selectedPlayerUuid = UUID.randomUUID();
        model.selectedWaypointId = UUID.randomUUID();

        model.clearNonBiomeDestinationSelection();

        assertEquals(DestinationMode.BIOME, model.getDestinationMode());
        assertNull(model.selectedPlayerUuid);
        assertNull(model.selectedWaypointId);
    }

    @Test
    void selectPlayer_null_clearsDestination() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            UUID playerId = UUID.randomUUID();
            assertTrue(TardisLogic.selectPlayer(tardisId, playerId, null));
            assertEquals(DestinationMode.PLAYER, model.getDestinationMode());
            assertEquals(playerId, model.selectedPlayerUuid);

            assertTrue(TardisLogic.selectPlayer(tardisId, null, null));
            assertEquals(DestinationMode.BIOME, model.getDestinationMode());
            assertNull(model.selectedPlayerUuid);
            assertNull(model.selectedWaypointId);
        }
    }

    @Test
    void getWaypoints_returnsCopy() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            WaypointLogic.add(model, "A");

            assertEquals(1, TardisLogic.getWaypoints(tardisId).size());
            assertTrue(TardisLogic.getWaypoints(UUID.randomUUID()).isEmpty());
        }
    }
}
