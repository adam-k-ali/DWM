package com.adamkali.dwm.guide;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldGuideBodyPaginatorTest {
    @Test
    void fitsOnFirstPage_singleRecipeSlice() {
        List<FieldGuideBodyPaginator.Slice> slices = FieldGuideBodyPaginator.paginate(4, 6, 12);
        assertEquals(List.of(new FieldGuideBodyPaginator.Slice(0, 4, true)), slices);
    }

    @Test
    void overflow_keepsRecipeOnFirstSliceAndDoesNotDropLines() {
        List<FieldGuideBodyPaginator.Slice> slices = FieldGuideBodyPaginator.paginate(17, 5, 8);
        assertEquals(3, slices.size());
        assertEquals(new FieldGuideBodyPaginator.Slice(0, 5, true), slices.get(0));
        assertEquals(new FieldGuideBodyPaginator.Slice(5, 8, false), slices.get(1));
        assertEquals(new FieldGuideBodyPaginator.Slice(13, 4, false), slices.get(2));
        assertEquals(17, slices.stream().mapToInt(FieldGuideBodyPaginator.Slice::count).sum());
        assertTrue(slices.getFirst().recipe());
        assertFalse(slices.get(1).recipe());
        assertFalse(slices.get(2).recipe());
    }

    @Test
    void emptyBody_keepsOneRecipeSlice() {
        List<FieldGuideBodyPaginator.Slice> slices = FieldGuideBodyPaginator.paginate(0, 5, 12);
        assertEquals(List.of(new FieldGuideBodyPaginator.Slice(0, 0, true)), slices);
    }

    @Test
    void bodyMaxRows_recipeWithIconsPathToggleAndPatternIsTighterThanTextOnly() {
        int cramped = FieldGuideBookLayout.bodyMaxRows(true, 5, true, true);
        int textOnly = FieldGuideBookLayout.bodyMaxRows(false, 0, false, false);
        assertEquals(1, cramped);
        assertEquals(15, textOnly);
        assertTrue(textOnly > cramped);
    }
}
