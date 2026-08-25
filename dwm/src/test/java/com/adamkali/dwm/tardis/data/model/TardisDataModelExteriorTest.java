package com.adamkali.dwm.tardis.data.model;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.StabiliserLogic;
import com.google.gson.Gson;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisDataModelExteriorTest {
    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
    }
    @Test
    void exteriorLocation_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.setExteriorLocation("minecraft:overworld", 10, 64, -3, 4);

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertTrue(loaded.hasExteriorLocation);
        assertEquals("minecraft:overworld", loaded.exteriorDimension);
        assertEquals(10, loaded.exteriorX);
        assertEquals(64, loaded.exteriorY);
        assertEquals(-3, loaded.exteriorZ);
        assertEquals(4, loaded.exteriorRotation);
        assertEquals(model, loaded);
    }

    @Test
    void selectedBiome_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.selectedBiome = "minecraft:plains";

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals("minecraft:plains", loaded.selectedBiome);
        assertEquals(model, loaded);
    }

    @Test
    void selectedDimension_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.selectedDimension = "minecraft:the_nether";

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals("minecraft:the_nether", loaded.selectedDimension);
        assertEquals(model, loaded);
    }

    @Test
    void travelFields_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelPhaseTicks = 40;
        model.travelDestinationBiome = "minecraft:forest";
        model.travelDestinationDimension = "minecraft:the_end";

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals(TardisTravelPhase.IN_FLIGHT, loaded.getTravelPhase());
        assertEquals(40, loaded.travelPhaseTicks);
        assertEquals("minecraft:forest", loaded.travelDestinationBiome);
        assertEquals("minecraft:the_end", loaded.travelDestinationDimension);
        assertEquals(model, loaded);
    }

    @Test
    void destinationModeAndWaypoints_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.setDestinationMode(DestinationMode.WAYPOINT);
        model.selectedPlayerUuid = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
        TardisWaypoint waypoint = new TardisWaypoint(
                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Pad",
                "minecraft:overworld",
                1,
                2,
                3,
                4
        );
        model.getWaypoints().add(waypoint);
        model.selectedWaypointId = waypoint.id;
        model.travelDestinationMode = DestinationMode.PLAYER;
        model.travelTargetPlayerUuid = model.selectedPlayerUuid;
        model.travelDestinationX = 9;

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals(DestinationMode.WAYPOINT, loaded.getDestinationMode());
        assertEquals(1, loaded.getWaypoints().size());
        assertEquals("Pad", loaded.getWaypoints().getFirst().name);
        assertEquals(waypoint.id, loaded.selectedWaypointId);
        assertEquals(DestinationMode.PLAYER, loaded.travelDestinationMode);
        assertEquals(9, loaded.travelDestinationX);
        assertEquals(model, loaded);
    }

    @Test
    void locationHistory_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.getLocationHistory().add(new TardisExteriorLocation("minecraft:overworld", 10, 64, -3, 4));
        model.getLocationHistory().add(new TardisExteriorLocation("minecraft:the_nether", 0, 70, 0, 2));
        model.selectedFastReturnIndex = 1;
        model.setDestinationMode(DestinationMode.FAST_RETURN);

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals(2, loaded.getLocationHistory().size());
        assertEquals("minecraft:overworld", loaded.getLocationHistory().getFirst().dimension);
        assertEquals(10, loaded.getLocationHistory().getFirst().x);
        assertEquals(1, loaded.selectedFastReturnIndex);
        assertEquals(DestinationMode.FAST_RETURN, loaded.getDestinationMode());
        assertEquals(model, loaded);
    }

    @Test
    void stabilisersEnabled_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        assertTrue(StabiliserLogic.isEnabled(model));
        model.stabilisersEnabled = false;

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertFalse(StabiliserLogic.isEnabled(loaded));
        assertEquals(model, loaded);

        TardisDataModel legacy = gson.fromJson("{\"uuid\":\"" + model.uuid + "\"}", TardisDataModel.class);
        assertTrue(StabiliserLogic.isEnabled(legacy), "missing field must default on");
    }

    @Test
    void cloakAndLocks_serializeThroughGsonAndDefaultOff() {
        TardisDataModel model = new TardisDataModel();
        model.cloaked = true;
        model.doorsLocked = true;
        model.lockX = true;
        model.lockZ = true;

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertTrue(loaded.cloaked);
        assertTrue(loaded.doorsLocked);
        assertTrue(loaded.lockX);
        assertFalse(loaded.lockY);
        assertTrue(loaded.lockZ);
        assertEquals(model, loaded);

        TardisDataModel legacy = gson.fromJson("{\"uuid\":\"" + model.uuid + "\"}", TardisDataModel.class);
        assertFalse(legacy.cloaked);
        assertFalse(legacy.doorsLocked);
        assertFalse(legacy.lockX);
        assertFalse(legacy.lockY);
        assertFalse(legacy.lockZ);
    }

    @Test
    void ownerUuid_SerializesThroughGsonAndLegacyNull() {
        TardisDataModel model = new TardisDataModel();
        UUID owner = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        model.setOwner(owner);

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals(owner, loaded.ownerUuid);
        assertEquals(model, loaded);

        TardisDataModel legacy = gson.fromJson("{\"uuid\":\"" + model.uuid + "\"}", TardisDataModel.class);
        assertNull(legacy.ownerUuid);
    }
}
