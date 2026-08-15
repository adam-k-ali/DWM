package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisExteriorLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FastReturnLogicTest {
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
        model = new TardisDataModel();
        model.uuid = UUID.randomUUID();
        model.setExteriorLocation("minecraft:overworld", 10, 64, -3, 0);
    }

    @Test
    void cycle_emptyHistoryFails() {
        assertTrue(FastReturnLogic.cycle(model).isEmpty());
        assertEquals(DestinationMode.BIOME, model.getDestinationMode());
    }

    @Test
    void cycle_firstClickSelectsNewest() {
        FastReturnLogic.pushDeparted(model);
        model.setExteriorLocation("minecraft:the_nether", 0, 70, 0, 2);

        Optional<TardisExteriorLocation> selected = FastReturnLogic.cycle(model);

        assertTrue(selected.isPresent());
        assertEquals(DestinationMode.FAST_RETURN, model.getDestinationMode());
        assertEquals(0, model.selectedFastReturnIndex);
        assertEquals("minecraft:overworld", selected.get().dimension);
        assertEquals(10, selected.get().x);
    }

    @Test
    void cycle_wrapsThroughHistory() {
        FastReturnLogic.pushDeparted(model);
        model.setExteriorLocation("minecraft:the_nether", 1, 70, 1, 0);
        FastReturnLogic.pushDeparted(model);
        model.setExteriorLocation("minecraft:the_end", 2, 80, 2, 0);

        assertEquals(1, FastReturnLogic.cycle(model).orElseThrow().x); // nether (newest)
        assertEquals(10, FastReturnLogic.cycle(model).orElseThrow().x); // overworld
        assertEquals(1, FastReturnLogic.cycle(model).orElseThrow().x); // wrap to nether
        assertEquals(0, model.selectedFastReturnIndex);
    }

    @Test
    void pushDeparted_capsAtSixteenAndSkipsConsecutiveDuplicate() {
        for (int i = 0; i < FastReturnLogic.MAX_HISTORY + 4; i++) {
            model.setExteriorLocation("minecraft:overworld", i, 64, 0, 0);
            assertTrue(FastReturnLogic.pushDeparted(model));
        }
        assertEquals(FastReturnLogic.MAX_HISTORY, model.getLocationHistory().size());
        assertEquals(FastReturnLogic.MAX_HISTORY + 3, model.getLocationHistory().getFirst().x);

        assertFalse(FastReturnLogic.pushDeparted(model));
        assertEquals(FastReturnLogic.MAX_HISTORY, model.getLocationHistory().size());
    }

    @Test
    void clearNonBiomeDestinationSelection_resetsFastReturnCursor() {
        FastReturnLogic.pushDeparted(model);
        FastReturnLogic.cycle(model);
        model.selectedFastReturnIndex = 0;
        model.setDestinationMode(DestinationMode.FAST_RETURN);

        model.clearNonBiomeDestinationSelection();

        assertEquals(DestinationMode.BIOME, model.getDestinationMode());
        assertEquals(0, model.selectedFastReturnIndex);
        assertFalse(FastReturnLogic.hasSelection(model));
    }

    @Test
    void resetIndexAfterLanding_onlyWhenFastReturnArmed() {
        FastReturnLogic.pushDeparted(model);
        model.setExteriorLocation("minecraft:the_nether", 1, 70, 1, 0);
        FastReturnLogic.pushDeparted(model);
        FastReturnLogic.cycle(model);
        FastReturnLogic.cycle(model);
        assertEquals(1, model.selectedFastReturnIndex);

        FastReturnLogic.resetIndexAfterLanding(model);
        assertEquals(0, model.selectedFastReturnIndex);

        model.setDestinationMode(DestinationMode.BIOME);
        model.selectedFastReturnIndex = 3;
        FastReturnLogic.resetIndexAfterLanding(model);
        assertEquals(3, model.selectedFastReturnIndex);
    }
}
