package com.adamkali.dwm.gui.layout;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.SpacerElement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StackTest {
    @Test
    void verticalStackPlacesChildrenWithGap() {
        SpacerElement first = new SpacerElement(10, 8);
        SpacerElement second = new SpacerElement(10, 8);
        SpacerElement third = new SpacerElement(10, 8);
        Stack stack = Stack.vertical(4);
        stack.add(first);
        stack.add(second);
        stack.add(third);
        stack.setPosition(20, 30);
        stack.arrangeElements();

        assertEquals(30, first.getY());
        assertEquals(42, second.getY());
        assertEquals(54, third.getY());
        assertEquals(20, first.getX());
        assertEquals(8 + 4 + 8 + 4 + 8, stack.getHeight());
    }

    @Test
    void columnsPlaceChildrenHorizontallyWithGap() {
        SpacerElement first = new SpacerElement(12, 6);
        SpacerElement second = new SpacerElement(12, 6);
        Stack columns = Columns.of(3, first, second);
        columns.setPosition(5, 9);
        columns.arrangeElements();

        assertEquals(5, first.getX());
        assertEquals(20, second.getX());
        assertEquals(9, first.getY());
        assertEquals(9, second.getY());
        assertEquals(12 + 3 + 12, columns.getWidth());
    }

    @Test
    void fillWidgetReportsConstructedSize() {
        FillWidget fill = new FillWidget(166, 1, 0xFFC79A45);
        assertEquals(166, fill.getWidth());
        assertEquals(1, fill.getHeight());
    }

    @Test
    void mountArrangesAndCollectsWidgets() {
        FillWidget hairline = new FillWidget(20, 1, 0xFFFFFFFF);
        SpacerElement spacer = new SpacerElement(20, 8);
        Stack stack = Stack.vertical(2);
        stack.add(hairline);
        stack.add(spacer);

        List<AbstractWidget> captured = new ArrayList<>();
        List<AbstractWidget> widgets = Layouts.mount(stack, 10, 15, captured::add);

        assertEquals(1, widgets.size());
        assertEquals(hairline, widgets.getFirst());
        assertEquals(captured, widgets);
        assertEquals(10, hairline.getX());
        assertEquals(15, hairline.getY());
        assertEquals(10, spacer.getX());
        assertEquals(18, spacer.getY());
    }
}
