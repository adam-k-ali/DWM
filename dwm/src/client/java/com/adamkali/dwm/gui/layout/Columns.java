package com.adamkali.dwm.gui.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.LayoutElement;

/**
 * Horizontal {@link Stack} factory for side-by-side children.
 */
@Environment(EnvType.CLIENT)
public final class Columns {
    private Columns() {
    }

    public static Stack of(int gap) {
        return Stack.horizontal(gap);
    }

    public static Stack of(int gap, LayoutElement... children) {
        Stack columns = Stack.horizontal(gap);
        for (LayoutElement child : children) {
            columns.add(child);
        }
        return columns;
    }
}
