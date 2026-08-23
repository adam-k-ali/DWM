package com.adamkali.sightline;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WidgetFinder {
    public Optional<AbstractWidget> find(Screen screen, Map<String, Object> selector) {
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

    public boolean matches(Screen screen, Map<String, Object> selector) {
        if ("screen".equals(selector.get("type"))) {
            return screen != null && selector.get("name").equals(screen.getClass().getSimpleName());
        }
        if (find(screen, selector).isPresent()) {
            return true;
        }
        String expectedName = (String) selector.get("name");
        return "label".equals(selector.get("type")) && expectedName.equals(connectScreenStatus(screen));
    }

    public List<String> visibleWidgets(Screen screen) {
        if (screen == null) {
            return List.of();
        }
        List<String> visible = new ArrayList<>();
        String connectStatus = connectScreenStatus(screen);
        if (connectStatus != null) {
            visible.add("label:" + connectStatus);
        }
        widgets(screen).stream()
                .filter(widget -> widget.visible)
                .map(widget -> widgetType(widget) + ":" + widget.getMessage().getString())
                .forEach(visible::add);
        return List.copyOf(visible);
    }

    public String describeVisibleWidgets(Screen screen) {
        StringBuilder dump = new StringBuilder();
        dump.append("debugScreen: ")
                .append(screen == null ? "<none>" : screen.getClass().getName())
                .append('\n');
        if (screen == null) {
            return dump.toString();
        }
        String connectStatus = connectScreenStatus(screen);
        int index = 0;
        if (connectStatus != null) {
            dump.append("  [").append(index++).append("] ")
                    .append("label name=\"").append(connectStatus).append("\"\n");
        }
        List<AbstractWidget> visible = widgets(screen).stream()
                .filter(widget -> widget.visible)
                .toList();
        for (AbstractWidget widget : visible) {
            dump.append("  [").append(index++).append("] ")
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

    private static String connectScreenStatus(Screen screen) {
        if (!(screen instanceof ConnectScreen connectScreen)) {
            return null;
        }
        Component status = connectScreen.status;
        if (status == null) {
            return null;
        }
        String text = status.getString();
        return text.isBlank() ? null : text;
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
            case "label" -> widget instanceof StringWidget;
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
        if (widget instanceof StringWidget) {
            return "label";
        }
        return widget.getClass().getSimpleName();
    }
}
