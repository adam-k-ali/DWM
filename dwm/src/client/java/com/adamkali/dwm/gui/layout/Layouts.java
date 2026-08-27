package com.adamkali.dwm.gui.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.Layout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Positions a layout tree and registers its interactive/paint widgets on a screen.
 */
@Environment(EnvType.CLIENT)
public final class Layouts {
    private Layouts() {
    }

    public static List<AbstractWidget> mount(Layout layout, int x, int y, Consumer<AbstractWidget> addWidget) {
        layout.setPosition(x, y);
        layout.arrangeElements();
        List<AbstractWidget> widgets = new ArrayList<>();
        layout.visitWidgets(widget -> {
            addWidget.accept(widget);
            widgets.add(widget);
        });
        return widgets;
    }
}
