package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisLogicDestinationTest {
    @Test
    void effectiveDestinationDimension_prefersSelectedThenExterior() {
        assertNull(TardisLogic.effectiveDestinationDimension(null));

        TardisDataModel model = new TardisDataModel();
        assertNull(TardisLogic.effectiveDestinationDimension(model));

        model.exteriorDimension = "minecraft:overworld";
        assertEquals("minecraft:overworld", TardisLogic.effectiveDestinationDimension(model));

        model.selectedDimension = "minecraft:the_nether";
        assertEquals("minecraft:the_nether", TardisLogic.effectiveDestinationDimension(model));

        model.selectedDimension = "   ";
        assertEquals("minecraft:overworld", TardisLogic.effectiveDestinationDimension(model));
    }
}
