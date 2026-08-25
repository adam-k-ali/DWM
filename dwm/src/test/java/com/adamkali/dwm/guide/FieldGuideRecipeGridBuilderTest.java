package com.adamkali.dwm.guide;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldGuideRecipeGridBuilderTest {
    @Test
    void shapedRecipeMapsToGridCoordinates() {
        List<FieldGuideRecipeGridBuilder.GridSlot> slots = FieldGuideRecipeGridBuilder.shapedSlotLayout(
                3,
                3,
                index -> index == 1 || index == 4 || index == 7
        );

        assertEquals(3, slots.size());
        assertTrue(slots.stream().anyMatch(slot -> slot.column() == 1 && slot.row() == 0));
        assertTrue(slots.stream().anyMatch(slot -> slot.column() == 1 && slot.row() == 1));
        assertTrue(slots.stream().anyMatch(slot -> slot.column() == 1 && slot.row() == 2));
    }

    @Test
    void shapelessRecipePacksLeftToRight() {
        List<FieldGuideRecipeGridBuilder.GridSlot> slots = FieldGuideRecipeGridBuilder.shapelessSlotLayout(3);

        assertEquals(3, slots.size());
        assertEquals(0, slots.get(0).column());
        assertEquals(0, slots.get(0).row());
        assertEquals(1, slots.get(1).column());
        assertEquals(0, slots.get(1).row());
        assertEquals(2, slots.get(2).column());
        assertEquals(0, slots.get(2).row());
    }
}
