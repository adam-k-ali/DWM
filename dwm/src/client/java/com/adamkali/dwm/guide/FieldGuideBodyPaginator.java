package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a wrapped body into visual pages. The first slice keeps the recipe; later slices are text-only.
 */
@Environment(EnvType.CLIENT)
public final class FieldGuideBodyPaginator {
    private FieldGuideBodyPaginator() {
    }

    public record Slice(int start, int count, boolean recipe) {
    }

    public static List<Slice> paginate(int lineCount, int firstPageRows, int continuationRows) {
        int first = Math.max(1, firstPageRows);
        int continuation = Math.max(1, continuationRows);
        int lines = Math.max(0, lineCount);
        List<Slice> slices = new ArrayList<>();
        int firstCount = Math.min(lines, first);
        slices.add(new Slice(0, firstCount, true));
        int start = firstCount;
        while (start < lines) {
            int count = Math.min(continuation, lines - start);
            slices.add(new Slice(start, count, false));
            start += count;
        }
        return List.copyOf(slices);
    }
}
