package com.adamkali.dwm.gui.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;

import java.util.function.Consumer;

/**
 * Vertical or horizontal sequence of {@link LayoutElement}s with a uniform gap.
 */
@Environment(EnvType.CLIENT)
public final class Stack implements Layout {
    private final LinearLayout inner;

    private Stack(LinearLayout inner) {
        this.inner = inner;
    }

    public static Stack vertical(int gap) {
        return new Stack(LinearLayout.vertical().spacing(gap));
    }

    public static Stack horizontal(int gap) {
        return new Stack(LinearLayout.horizontal().spacing(gap));
    }

    public <T extends LayoutElement> T add(T child) {
        return inner.addChild(child);
    }

    public <T extends LayoutElement> T add(T child, Consumer<LayoutSettings> settings) {
        return inner.addChild(child, settings);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        inner.visitChildren(visitor);
    }

    @Override
    public void removeChildren() {
        inner.removeChildren();
    }

    @Override
    public void arrangeElements() {
        inner.arrangeElements();
    }

    @Override
    public int getWidth() {
        return inner.getWidth();
    }

    @Override
    public int getHeight() {
        return inner.getHeight();
    }

    @Override
    public void setX(int x) {
        inner.setX(x);
    }

    @Override
    public void setY(int y) {
        inner.setY(y);
    }

    @Override
    public int getX() {
        return inner.getX();
    }

    @Override
    public int getY() {
        return inner.getY();
    }
}
