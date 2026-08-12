package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.WaypointLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OpenWaypointScreenTest {
    private UUID tardisId;
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
        tardisId = UUID.randomUUID();
        model = new TardisDataModel();
    }

    @Test
    void of_includesExteriorSnapshotWhenPresent() {
        model.setExteriorLocation("minecraft:overworld", 10, 64, -20, 2);

        OpenWaypointScreen screen = OpenWaypointScreen.of(tardisId, model);

        assertTrue(screen.canSave());
        assertNotNull(screen.exteriorLocation());
        assertEquals("minecraft:overworld", screen.exteriorLocation().dimension());
        assertEquals(10, screen.exteriorLocation().x());
        assertEquals(64, screen.exteriorLocation().y());
        assertEquals(-20, screen.exteriorLocation().z());
    }

    @Test
    void of_omitsExteriorSnapshotWithoutExterior() {
        OpenWaypointScreen screen = OpenWaypointScreen.of(tardisId, model);

        assertFalse(screen.canSave());
        assertNull(screen.exteriorLocation());
        assertNull(OpenWaypointScreen.exteriorLocationOf(model));
    }

    @Test
    void of_omitsExteriorSnapshotForNullModel() {
        OpenWaypointScreen screen = OpenWaypointScreen.of(tardisId, (TardisDataModel) null);

        assertFalse(screen.canSave());
        assertTrue(screen.waypoints().isEmpty());
        assertNull(screen.exteriorLocation());
    }

    @Test
    void of_disablesSaveAtWaypointCapButKeepsExteriorSnapshot() {
        model.setExteriorLocation("dwm:gallifrey", 1, 2, 3, 0);
        for (int i = 0; i < WaypointLogic.MAX_WAYPOINTS; i++) {
            assertTrue(WaypointLogic.add(model, "WP" + i).isPresent());
        }

        OpenWaypointScreen screen = OpenWaypointScreen.of(tardisId, model);

        assertFalse(screen.canSave());
        assertEquals(WaypointLogic.MAX_WAYPOINTS, screen.waypoints().size());
        assertNotNull(screen.exteriorLocation());
        assertEquals("dwm:gallifrey", screen.exteriorLocation().dimension());
    }

    @Test
    void of_setsLocationWaypointIdWhenAtExterior() {
        model.setExteriorLocation("minecraft:overworld", 10, 64, -20, 0);
        var created = WaypointLogic.add(model, "Here").orElseThrow();

        OpenWaypointScreen screen = OpenWaypointScreen.of(tardisId, model);

        assertEquals(created.id, screen.locationWaypointId());
    }
}
