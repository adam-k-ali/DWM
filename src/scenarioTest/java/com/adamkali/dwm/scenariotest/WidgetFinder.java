package com.adamkali.dwm.scenariotest;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class WidgetFinder {
    Optional<AbstractWidget> find(Screen screen, Map<String, Object> selector) {
        if (screen == null) {
            return Optional.empty();
        }
        String expectedType = (String) selector.get("type");
        String expectedName = (String) selector.get("name");
        return widgets(screen).stream()
                .filter(widget -> matchesType(widget, expectedType))
                .filter(widget -> widget.visible)
                .filter(widget -> expectedName.equals(widget.getMessage().getString()))
                .findFirst();
    }

    List<String> visibleWidgets(Screen screen) {
        if (screen == null) {
            return List.of();
        }
        return widgets(screen).stream()
                .filter(widget -> widget.visible)
                .map(widget -> widgetType(widget) + ":" + widget.getMessage().getString())
                .toList();
    }

    String describeVisibleWidgets(Screen screen) {
        StringBuilder dump = new StringBuilder();
        dump.append("debugScreen: ")
                .append(screen == null ? "<none>" : screen.getClass().getName())
                .append('\n');
        if (screen == null) {
            return dump.toString();
        }
        List<AbstractWidget> visible = widgets(screen).stream()
                .filter(widget -> widget.visible)
                .toList();
        for (int i = 0; i < visible.size(); i++) {
            AbstractWidget widget = visible.get(i);
            dump.append("  [").append(i).append("] ")
                    .append(widgetType(widget))
                    .append(" name=\"").append(widget.getMessage().getString()).append('"')
                    .append(" active=").append(widget.active)
                    .append(" bounds=(")
                    .append(widget.getX()).append(',')
                    .append(widget.getY()).append(',')
                    .append(widget.getWidth()).append(',')
                    .append(widget.getHeight())
                    .append(")\n");
        }
        return dump.toString();
    }

    private static List<AbstractWidget> widgets(ContainerEventHandler root) {
        List<AbstractWidget> widgets = new ArrayList<>();
        collectWidgets(root.children(), widgets);
        return widgets;
    }

    private static void collectWidgets(
            List<? extends GuiEventListener> children,
            List<AbstractWidget> widgets
    ) {
        for (GuiEventListener child : children) {
            if (child instanceof AbstractWidget widget) {
                widgets.add(widget);
            }
            if (child instanceof ContainerEventHandler container) {
                collectWidgets(container.children(), widgets);
            }
        }
    }

    private static boolean matchesType(AbstractWidget widget, String expectedType) {
        return switch (expectedType) {
            case "button" -> widget instanceof Button;
            case "cycle" -> widget instanceof CycleButton<?>;
            case "tab" -> widget instanceof TabButton;
            case "editbox" -> widget instanceof EditBox;
            default -> false;
        };
    }

    private static String widgetType(AbstractWidget widget) {
        if (widget instanceof TabButton) {
            return "tab";
        }
        if (widget instanceof Button) {
            return "button";
        }
        if (widget instanceof CycleButton<?>) {
            return "cycle";
        }
        if (widget instanceof EditBox) {
            return "editbox";
        }
        return widget.getClass().getSimpleName();
    }
}
