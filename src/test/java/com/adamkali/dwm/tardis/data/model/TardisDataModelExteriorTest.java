package com.adamkali.dwm.tardis.data.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisDataModelExteriorTest {
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
    void travelFields_SerializesThroughGson() {
        TardisDataModel model = new TardisDataModel();
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelPhaseTicks = 40;
        model.travelDestinationBiome = "minecraft:forest";

        Gson gson = new Gson();
        TardisDataModel loaded = gson.fromJson(gson.toJson(model), TardisDataModel.class);

        assertEquals(TardisTravelPhase.IN_FLIGHT, loaded.getTravelPhase());
        assertEquals(40, loaded.travelPhaseTicks);
        assertEquals("minecraft:forest", loaded.travelDestinationBiome);
        assertEquals(model, loaded);
    }
}
