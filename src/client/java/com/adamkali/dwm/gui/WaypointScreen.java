package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.network.OpenWaypointScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WaypointScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");

    private final ClientTardis tardis;
    private final List<OpenWaypointScreen.WaypointEntry> waypoints;
    private final boolean canSave;
    private int selectedIndex = -1;

    public WaypointScreen(ClientTardis tardis, List<OpenWaypointScreen.WaypointEntry> waypoints, boolean canSave) {
        super(Component.translatable("dwm.gui.waypoint.title"));
        this.tardis = tardis;
        this.waypoints = new ArrayList<>(waypoints);
        this.canSave = canSave;
    }

    @Override
    protected void init() {
        int panelLeft = width / 2 - 120;
        int panelTop = height / 2 - 90;
        int y = panelTop + 30;
        for (int i = 0; i < waypoints.size(); i++) {
            final int index = i;
            OpenWaypointScreen.WaypointEntry entry = waypoints.get(i);
            addRenderableWidget(Button.builder(Component.literal(entry.name()), button -> selectedIndex = index)
                    .bounds(panelLeft + 10, y, 180, 20)
                    .build());
            y += 22;
        }

        Button saveButton = Button.builder(Component.translatable("dwm.gui.waypoint.save"), button -> {
            tardis.saveWaypoint("");
            onClose();
        }).bounds(panelLeft + 10, height / 2 + 70, 70, 20).build();
        saveButton.active = canSave;

        Button selectButton = Button.builder(Component.translatable("dwm.gui.waypoint.select"), button -> {
            UUID id = selectedWaypointId();
            if (id != null) {
                tardis.selectWaypoint(id);
                onClose();
            }
        }).bounds(panelLeft + 85, height / 2 + 70, 70, 20).build();

        Button deleteButton = Button.builder(Component.translatable("dwm.gui.waypoint.delete"), button -> {
            UUID id = selectedWaypointId();
            if (id != null) {
                tardis.deleteWaypoint(id);
                onClose();
            }
        }).bounds(panelLeft + 160, height / 2 + 70, 70, 20).build();

        Button doneButton = Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 10, height / 2 + 95, 220, 20)
                .build();

        addRenderableWidget(saveButton);
        addRenderableWidget(selectButton);
        addRenderableWidget(deleteButton);
        addRenderableWidget(doneButton);
    }

    private UUID selectedWaypointId() {
        if (selectedIndex < 0 || selectedIndex >= waypoints.size()) {
            return null;
        }
        return waypoints.get(selectedIndex).id();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x1 = width / 2 - 128;
        int y1 = height / 2 - 110;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x1, y1, 256, 230);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(font, getTitle(), x1 + 10, y1 + 10, 0x404040, false);
        if (waypoints.isEmpty()) {
            graphics.centeredText(font, Component.translatable("dwm.gui.waypoint.empty"), width / 2, height / 2 - 20, 0xFFFFFF);
        }
        if (selectedIndex >= 0 && selectedIndex < waypoints.size()) {
            OpenWaypointScreen.WaypointEntry entry = waypoints.get(selectedIndex);
            graphics.centeredText(
                    font,
                    Component.literal(entry.dimension() + " @ " + entry.x() + ", " + entry.y() + ", " + entry.z()),
                    width / 2,
                    height / 2 + 55,
                    0xA0A0A0
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
