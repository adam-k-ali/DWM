package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.network.OpenWaypointScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WaypointScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");
    private static final Identifier ICON_SELECTED =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/selected");
    private static final Identifier ICON_AT_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/at_location");
    private static final Identifier ICON_NEW =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/new");
    private static final Identifier ICON_EDIT =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/edit");
    private static final Identifier ICON_DELETE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/delete");
    private static final Identifier ICON_CLEAR =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/clear");
    private static final int MAX_NAME_LENGTH = 32;
    private static final int ROW_HEIGHT = 22;
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 200;
    private static final int LIST_WIDTH = 136;
    private static final int DETAIL_WIDTH = 136;
    private static final int BODY_HEIGHT = 118;
    private static final int ICON_SIZE = 10;
    private static final int ICON_GAP = 2;
    private static final int ACTION_BUTTON_SIZE = 20;
    private static final int ACTION_SPRITE_SIZE = 12;

    private final ClientTardis tardis;
    private final List<OpenWaypointScreen.WaypointEntry> waypoints;
    private final boolean canSave;
    private @Nullable UUID destinationWaypointId;
    private final @Nullable UUID locationWaypointId;

    private WaypointList list;
    private SpriteIconButton editButton;
    private SpriteIconButton deleteButton;
    private SpriteIconButton selectDestinationButton;
    private SpriteIconButton clearDestinationButton;
    private SpriteIconButton newButton;
    private Button doneButton;
    private EditBox nameField;
    private Button confirmButton;
    private Button cancelButton;

    private int panelLeft;
    private int panelTop;
    private int detailLeft;
    private int detailTop;

    private NameMode nameMode = NameMode.LIST;
    private @Nullable UUID renameTargetId;
    private @Nullable UUID selectedId;

    public WaypointScreen(
            ClientTardis tardis,
            List<OpenWaypointScreen.WaypointEntry> waypoints,
            boolean canSave,
            @Nullable UUID destinationWaypointId,
            @Nullable UUID locationWaypointId
    ) {
        super(Component.translatable("dwm.gui.waypoint.title"));
        this.tardis = tardis;
        this.waypoints = new ArrayList<>(waypoints);
        this.canSave = canSave;
        this.destinationWaypointId = destinationWaypointId;
        this.locationWaypointId = locationWaypointId;
        if (!this.waypoints.isEmpty()) {
            this.selectedId = this.waypoints.getFirst().id();
        }
    }

    @Override
    protected void init() {
        panelLeft = width / 2 - PANEL_WIDTH / 2;
        panelTop = height / 2 - PANEL_HEIGHT / 2;
        int bodyTop = panelTop + 28;
        detailLeft = panelLeft + 10 + LIST_WIDTH + 6;
        detailTop = bodyTop;

        int detailButtonY = detailTop + BODY_HEIGHT - 26;
        int detailActionsLeft = detailLeft + 4;

        editButton = iconButton(
                Component.translatable("dwm.gui.waypoint.edit"),
                Component.translatable("dwm.gui.waypoint.tooltip.edit"),
                ICON_EDIT,
                button -> {
                    OpenWaypointScreen.WaypointEntry selected = selectedWaypoint();
                    if (selected != null) {
                        beginRename(selected);
                    }
                }
        );
        editButton.setPosition(detailActionsLeft, detailButtonY);

        deleteButton = iconButton(
                Component.translatable("dwm.gui.waypoint.delete"),
                Component.translatable("dwm.gui.waypoint.tooltip.delete"),
                ICON_DELETE,
                button -> {
                    OpenWaypointScreen.WaypointEntry selected = selectedWaypoint();
                    if (selected != null) {
                        deleteWaypoint(selected);
                    }
                }
        );
        deleteButton.setPosition(detailActionsLeft + ACTION_BUTTON_SIZE + 4, detailButtonY);

        selectDestinationButton = iconButton(
                Component.translatable("dwm.gui.waypoint.select"),
                Component.translatable("dwm.gui.waypoint.tooltip.select"),
                ICON_SELECTED,
                button -> selectCurrent()
        );
        selectDestinationButton.setPosition(detailLeft + DETAIL_WIDTH - ACTION_BUTTON_SIZE - 4, detailButtonY);

        clearDestinationButton = iconButton(
                Component.translatable("dwm.gui.waypoint.clear"),
                Component.translatable("dwm.gui.waypoint.tooltip.clear"),
                ICON_CLEAR,
                button -> clearDestination()
        );
        clearDestinationButton.setPosition(detailLeft + DETAIL_WIDTH - ACTION_BUTTON_SIZE - 4, detailButtonY);

        int footerY = panelTop + PANEL_HEIGHT - 40;
        newButton = iconButton(
                Component.translatable("dwm.gui.waypoint.new"),
                Component.translatable("dwm.gui.waypoint.tooltip.new"),
                ICON_NEW,
                button -> beginCreate()
        );
        newButton.setPosition(panelLeft + 10, footerY);
        newButton.active = canSave;

        doneButton = Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 38, footerY, PANEL_WIDTH - 48, 20)
                .build();

        nameField = new EditBox(
                font,
                panelLeft + 10,
                footerY - 24,
                PANEL_WIDTH - 20,
                20,
                Component.translatable("dwm.gui.waypoint.name")
        );
        nameField.setMaxLength(MAX_NAME_LENGTH);
        nameField.setHint(Component.translatable("dwm.gui.waypoint.name"));

        int nameButtonWidth = (PANEL_WIDTH - 30) / 2;
        confirmButton = Button.builder(Component.translatable("dwm.gui.waypoint.confirm"), button -> confirmName())
                .bounds(panelLeft + 10, footerY, nameButtonWidth, 20)
                .build();

        cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> exitNameMode())
                .bounds(panelLeft + 20 + nameButtonWidth, footerY, nameButtonWidth, 20)
                .build();

        list = new WaypointList(minecraft, LIST_WIDTH, BODY_HEIGHT, bodyTop, ROW_HEIGHT);
        list.updateSizeAndPosition(LIST_WIDTH, BODY_HEIGHT, panelLeft + 10, bodyTop);
        rebuildListEntries();

        addRenderableWidget(list);
        addRenderableWidget(editButton);
        addRenderableWidget(deleteButton);
        addRenderableWidget(selectDestinationButton);
        addRenderableWidget(clearDestinationButton);
        addRenderableWidget(newButton);
        addRenderableWidget(doneButton);
        addRenderableWidget(nameField);
        addRenderableWidget(confirmButton);
        addRenderableWidget(cancelButton);

        applyModeVisibility();
        updateDetailActions();
    }

    private static SpriteIconButton iconButton(
            Component message,
            Component tooltip,
            Identifier sprite,
            Button.OnPress onPress
    ) {
        return SpriteIconButton.builder(message, onPress, true)
                .size(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE)
                .sprite(sprite, ACTION_SPRITE_SIZE, ACTION_SPRITE_SIZE)
                .tooltip(tooltip)
                .build();
    }

    private void rebuildListEntries() {
        UUID keepId = selectedId;
        list.clearAll();
        WaypointList.WaypointEntryRow toSelect = null;
        for (OpenWaypointScreen.WaypointEntry entry : waypoints) {
            WaypointList.WaypointEntryRow row = list.new WaypointEntryRow(entry);
            list.addRow(row);
            if (keepId != null && keepId.equals(entry.id())) {
                toSelect = row;
            }
        }
        if (toSelect != null) {
            list.setSelected(toSelect);
            selectedId = toSelect.waypoint.id();
        } else if (!list.children().isEmpty()) {
            WaypointList.WaypointEntryRow first = list.children().getFirst();
            list.setSelected(first);
            selectedId = first.waypoint.id();
        } else {
            list.setSelected(null);
            selectedId = null;
        }
        updateDetailActions();
    }

    private @Nullable OpenWaypointScreen.WaypointEntry selectedWaypoint() {
        if (selectedId == null) {
            return null;
        }
        for (OpenWaypointScreen.WaypointEntry entry : waypoints) {
            if (selectedId.equals(entry.id())) {
                return entry;
            }
        }
        return null;
    }

    private void onRowSelected(OpenWaypointScreen.WaypointEntry entry) {
        selectedId = entry.id();
        updateDetailActions();
    }

    private void updateDetailActions() {
        if (editButton == null || deleteButton == null
                || selectDestinationButton == null || clearDestinationButton == null) {
            return;
        }
        boolean hasSelection = selectedWaypoint() != null && nameMode == NameMode.LIST;
        boolean isDestination = hasSelection
                && destinationWaypointId != null
                && destinationWaypointId.equals(selectedId);

        editButton.active = hasSelection;
        deleteButton.active = hasSelection;
        selectDestinationButton.active = hasSelection && !isDestination;
        clearDestinationButton.active = hasSelection && isDestination;

        selectDestinationButton.visible = nameMode == NameMode.LIST && hasSelection && !isDestination;
        clearDestinationButton.visible = nameMode == NameMode.LIST && hasSelection && isDestination;
    }

    private void beginCreate() {
        if (!canSave) {
            return;
        }
        nameMode = NameMode.CREATE;
        renameTargetId = null;
        nameField.setValue("");
        applyModeVisibility();
        setInitialFocus(nameField);
    }

    private void beginRename(OpenWaypointScreen.WaypointEntry entry) {
        nameMode = NameMode.RENAME;
        renameTargetId = entry.id();
        nameField.setValue(entry.name() == null ? "" : entry.name());
        applyModeVisibility();
        setInitialFocus(nameField);
    }

    private void exitNameMode() {
        nameMode = NameMode.LIST;
        renameTargetId = null;
        nameField.setValue("");
        applyModeVisibility();
        updateDetailActions();
    }

    private void confirmName() {
        String name = nameField.getValue() == null ? "" : nameField.getValue().trim();
        if (name.isEmpty()) {
            return;
        }
        if (nameMode == NameMode.CREATE) {
            tardis.saveWaypoint(name);
            onClose();
            return;
        }
        if (nameMode == NameMode.RENAME && renameTargetId != null) {
            UUID id = renameTargetId;
            tardis.renameWaypoint(id, name);
            for (int i = 0; i < waypoints.size(); i++) {
                OpenWaypointScreen.WaypointEntry entry = waypoints.get(i);
                if (id.equals(entry.id())) {
                    waypoints.set(i, new OpenWaypointScreen.WaypointEntry(
                            entry.id(),
                            name,
                            entry.dimension(),
                            entry.x(),
                            entry.y(),
                            entry.z(),
                            entry.rotation()
                    ));
                    break;
                }
            }
            selectedId = id;
            rebuildListEntries();
            exitNameMode();
        }
    }

    private void deleteWaypoint(OpenWaypointScreen.WaypointEntry entry) {
        tardis.deleteWaypoint(entry.id());
        waypoints.removeIf(w -> entry.id().equals(w.id()));
        if (entry.id().equals(selectedId)) {
            selectedId = null;
        }
        if (entry.id().equals(destinationWaypointId)) {
            destinationWaypointId = null;
        }
        rebuildListEntries();
        if (nameMode == NameMode.RENAME && entry.id().equals(renameTargetId)) {
            exitNameMode();
        }
    }

    private void selectCurrent() {
        OpenWaypointScreen.WaypointEntry selected = selectedWaypoint();
        if (selected == null) {
            return;
        }
        tardis.selectWaypoint(selected.id());
        destinationWaypointId = selected.id();
        updateDetailActions();
    }

    private void clearDestination() {
        tardis.selectWaypoint(null);
        destinationWaypointId = null;
        updateDetailActions();
    }

    private void applyModeVisibility() {
        boolean naming = nameMode != NameMode.LIST;
        list.visible = !naming;
        editButton.visible = !naming;
        deleteButton.visible = !naming;
        newButton.visible = !naming;
        doneButton.visible = !naming;
        nameField.visible = naming;
        confirmButton.visible = naming;
        cancelButton.visible = naming;
        if (naming) {
            selectDestinationButton.visible = false;
            clearDestinationButton.visible = false;
            nameField.setEditable(true);
        }
        updateDetailActions();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x1 = width / 2 - PANEL_WIDTH / 2;
        int y1 = height / 2 - PANEL_HEIGHT / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x1, y1, PANEL_WIDTH, PANEL_HEIGHT);

        if (nameMode == NameMode.LIST) {
            // Draw behind widgets so action buttons stay clickable and visible.
            graphics.fill(detailLeft, detailTop, detailLeft + DETAIL_WIDTH, detailTop + BODY_HEIGHT, 0xFF333333);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(font, getTitle(), x1 + 10, y1 + 10, 0xFFFFFFFF, false);

        if (nameMode != NameMode.LIST) {
            return;
        }

        if (waypoints.isEmpty()) {
            graphics.centeredText(
                    font,
                    Component.translatable("dwm.gui.waypoint.empty"),
                    width / 2,
                    detailTop + BODY_HEIGHT / 2 - 4,
                    0xFFFFFFFF
            );
            return;
        }

        OpenWaypointScreen.WaypointEntry selected = selectedWaypoint();
        if (selected == null) {
            return;
        }

        int textX = detailLeft + 8;
        graphics.text(font, Component.translatable("dwm.gui.waypoint.detail.name"), textX, detailTop + 8, 0xFF888888, false);
        graphics.text(font, Component.literal(selected.name()), textX, detailTop + 20, 0xFFFFFFFF, false);

        graphics.text(font, Component.translatable("dwm.gui.waypoint.detail.location"), textX, detailTop + 44, 0xFF888888, false);
        graphics.text(font, Component.literal(selected.dimension()), textX, detailTop + 56, 0xFFFFFFFF, false);
        String coords = selected.x() + ", " + selected.y() + ", " + selected.z();
        graphics.text(font, Component.literal(coords), textX, detailTop + 68, 0xFFA0A0A0, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum NameMode {
        LIST,
        CREATE,
        RENAME
    }

    private class WaypointList extends ObjectSelectionList<WaypointList.WaypointEntryRow> {
        WaypointList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        void clearAll() {
            clearEntries();
        }

        int addRow(WaypointEntryRow entry) {
            return addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH - 8;
        }

        @Override
        public void setSelected(@Nullable WaypointEntryRow entry) {
            super.setSelected(entry);
            if (entry != null) {
                onRowSelected(entry.waypoint);
            }
        }

        class WaypointEntryRow extends ObjectSelectionList.Entry<WaypointEntryRow> {
            private final OpenWaypointScreen.WaypointEntry waypoint;

            WaypointEntryRow(OpenWaypointScreen.WaypointEntry waypoint) {
                this.waypoint = waypoint;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
                boolean isDestination = destinationWaypointId != null && destinationWaypointId.equals(waypoint.id());
                boolean isAtLocation = locationWaypointId != null && locationWaypointId.equals(waypoint.id());
                int iconCount = (isDestination ? 1 : 0) + (isAtLocation ? 1 : 0);
                int iconsWidth = iconCount == 0 ? 0 : iconCount * ICON_SIZE + (iconCount - 1) * ICON_GAP + 4;

                int textColor = isFocused() || WaypointList.this.getSelected() == this ? 0xFFFFFFFF : 0xFFE0E0E0;
                graphics.text(
                        font,
                        Component.literal(waypoint.name()),
                        getContentX() + 4,
                        getContentYMiddle() - 4,
                        textColor,
                        false
                );

                if (iconCount == 0) {
                    return;
                }
                int iconX = getContentRight() - iconsWidth;
                int iconY = getContentYMiddle() - ICON_SIZE / 2;
                if (isAtLocation) {
                    graphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            ICON_AT_LOCATION,
                            iconX,
                            iconY,
                            ICON_SIZE,
                            ICON_SIZE
                    );
                    if (isMouseOverIcon(mouseX, mouseY, iconX, iconY)) {
                        graphics.setTooltipForNextFrame(
                                Component.translatable("dwm.gui.waypoint.tooltip.at_location"),
                                mouseX,
                                mouseY
                        );
                    }
                    iconX += ICON_SIZE + ICON_GAP;
                }
                if (isDestination) {
                    graphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            ICON_SELECTED,
                            iconX,
                            iconY,
                            ICON_SIZE,
                            ICON_SIZE
                    );
                    if (isMouseOverIcon(mouseX, mouseY, iconX, iconY)) {
                        graphics.setTooltipForNextFrame(
                                Component.translatable("dwm.gui.waypoint.tooltip.destination"),
                                mouseX,
                                mouseY
                        );
                    }
                }
            }

            private static boolean isMouseOverIcon(int mouseX, int mouseY, int iconX, int iconY) {
                return mouseX >= iconX && mouseX < iconX + ICON_SIZE
                        && mouseY >= iconY && mouseY < iconY + ICON_SIZE;
            }

            @Override
            public Component getNarration() {
                return Component.literal(waypoint.name());
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                WaypointList.this.setSelected(this);
                return true;
            }
        }
    }
}
