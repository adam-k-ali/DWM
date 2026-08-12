package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WaypointLogicTest {
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
        model = new TardisDataModel();
        model.setExteriorLocation("minecraft:overworld", 10, 64, -20, 2);
    }

    @Test
    void add_createsFromExteriorWithGeneratedName() {
        Optional<TardisWaypoint> created = WaypointLogic.add(model, null);

        assertTrue(created.isPresent());
        assertEquals("Waypoint 1", created.get().name);
        assertEquals("minecraft:overworld", created.get().dimension);
        assertEquals(10, created.get().x);
        assertEquals(64, created.get().y);
        assertEquals(-20, created.get().z);
        assertEquals(2, created.get().rotation);
        assertEquals(1, model.getWaypoints().size());
    }

    @Test
    void add_rejectsDuplicateNamesCaseInsensitive() {
        assertTrue(WaypointLogic.add(model, "Home").isPresent());
        assertTrue(WaypointLogic.add(model, "home").isEmpty());
        assertEquals(1, model.getWaypoints().size());
    }

    @Test
    void add_rejectsWithoutExterior() {
        TardisDataModel empty = new TardisDataModel();
        assertTrue(WaypointLogic.add(empty, "A").isEmpty());
    }

    @Test
    void add_enforcesCap() {
        for (int i = 0; i < WaypointLogic.MAX_WAYPOINTS; i++) {
            assertTrue(WaypointLogic.add(model, "WP" + i).isPresent());
        }
        assertTrue(WaypointLogic.add(model, "overflow").isEmpty());
        assertEquals(WaypointLogic.MAX_WAYPOINTS, model.getWaypoints().size());
    }

    @Test
    void select_setsModeAndClearsPlayer() {
        TardisWaypoint waypoint = WaypointLogic.add(model, "Pad").orElseThrow();
        model.selectedPlayerUuid = UUID.randomUUID();
        model.setDestinationMode(DestinationMode.PLAYER);

        assertTrue(WaypointLogic.select(model, waypoint.id));
        assertEquals(DestinationMode.WAYPOINT, model.getDestinationMode());
        assertEquals(waypoint.id, model.selectedWaypointId);
        assertNull(model.selectedPlayerUuid);
    }

    @Test
    void select_rejectsUnknownId() {
        assertFalse(WaypointLogic.select(model, UUID.randomUUID()));
        assertEquals(DestinationMode.BIOME, model.getDestinationMode());
    }

    @Test
    void delete_removesAndClearsSelection() {
        TardisWaypoint waypoint = WaypointLogic.add(model, "Pad").orElseThrow();
        WaypointLogic.select(model, waypoint.id);

        assertTrue(WaypointLogic.delete(model, waypoint.id));
        assertTrue(model.getWaypoints().isEmpty());
        assertNull(model.selectedWaypointId);
        assertEquals(DestinationMode.BIOME, model.getDestinationMode());
    }

    @Test
    void rename_updatesName() {
        TardisWaypoint waypoint = WaypointLogic.add(model, "Pad").orElseThrow();

        assertTrue(WaypointLogic.rename(model, waypoint.id, "Home"));
        assertEquals("Home", waypoint.name);
    }

    @Test
    void rename_rejectsBlankAndUnknownId() {
        TardisWaypoint waypoint = WaypointLogic.add(model, "Pad").orElseThrow();

        assertFalse(WaypointLogic.rename(model, waypoint.id, "  "));
        assertFalse(WaypointLogic.rename(model, waypoint.id, null));
        assertFalse(WaypointLogic.rename(model, UUID.randomUUID(), "Elsewhere"));
        assertEquals("Pad", waypoint.name);
    }

    @Test
    void rename_rejectsDuplicateNamesCaseInsensitive() {
        TardisWaypoint first = WaypointLogic.add(model, "Home").orElseThrow();
        TardisWaypoint second = WaypointLogic.add(model, "Pad").orElseThrow();

        assertFalse(WaypointLogic.rename(model, second.id, "home"));
        assertEquals("Pad", second.name);
        assertEquals("Home", first.name);
    }

    @Test
    void rename_allowsSameNameKeepingIdentity() {
        TardisWaypoint waypoint = WaypointLogic.add(model, "Home").orElseThrow();

        assertTrue(WaypointLogic.rename(model, waypoint.id, "Home"));
        assertTrue(WaypointLogic.rename(model, waypoint.id, "HOME"));
        assertEquals("HOME", waypoint.name);
    }

    @Test
    void waypoints_accessorMatchesModelList() {
        WaypointLogic.add(model, "A");
        assertSame(model.getWaypoints(), WaypointLogic.waypoints(model));
        assertTrue(WaypointLogic.waypoints(null).isEmpty());
    }
}
