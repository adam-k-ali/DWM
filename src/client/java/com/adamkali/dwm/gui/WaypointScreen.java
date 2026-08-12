package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.network.OpenWaypointScreen;
import com.adamkali.dwm.text.DimensionNames;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
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
    /** Client-only sentinel for the create-mode ghost list row. */
    private static final UUID GHOST_ROW_ID = new UUID(0L, 0L);
    /** Client-only sentinel for the empty-list hint row under the ghost. */
    private static final UUID EMPTY_HINT_ROW_ID = new UUID(0L, 1L);
    private static final int GHOST_TEXT_COLOR = 0xFF9CCC6A;
    private static final int GHOST_TEXT_COLOR_MUTED = 0xFF7AAA50;
    private static final int GHOST_TEXT_COLOR_DISABLED = 0xFF4A5A40;
    private static final int EMPTY_HINT_TEXT_COLOR = 0xFF888888;
    private static final int MODE_LABEL_COLOR = 0xFFD4A84B;
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
    private static final int DETAIL_BUTTON_WIDTH = 62;

    private final ClientTardis tardis;
    private final List<OpenWaypointScreen.WaypointEntry> waypoints;
    private final boolean canSave;
    private @Nullable UUID destinationWaypointId;
    private @Nullable UUID locationWaypointId;
    private final @Nullable OpenWaypointScreen.ExteriorLocation exteriorLocation;

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
    private boolean createPending;

    public WaypointScreen(
            ClientTardis tardis,
            List<OpenWaypointScreen.WaypointEntry> waypoints,
            boolean canSave,
            @Nullable UUID destinationWaypointId,
            @Nullable UUID locationWaypointId,
            @Nullable OpenWaypointScreen.ExteriorLocation exteriorLocation
    ) {
        super(Component.translatable("dwm.gui.waypoint.title"));
        this.tardis = tardis;
        this.waypoints = new ArrayList<>(waypoints);
        this.canSave = canSave;
        this.destinationWaypointId = destinationWaypointId;
        this.locationWaypointId = locationWaypointId;
        this.exteriorLocation = exteriorLocation;
        this.selectedId = preferSelectedId();
    }

    private @Nullable UUID preferSelectedId() {
        if (containsWaypoint(locationWaypointId)) {
            return locationWaypointId;
        }
        if (containsWaypoint(destinationWaypointId)) {
            return destinationWaypointId;
        }
        if (!waypoints.isEmpty()) {
            return waypoints.getFirst().id();
        }
        return null;
    }

    private boolean containsWaypoint(@Nullable UUID id) {
        if (id == null) {
            return false;
        }
        for (OpenWaypointScreen.WaypointEntry entry : waypoints) {
            if (id.equals(entry.id())) {
                return true;
            }
        }
        return false;
    }

    /** True when a new waypoint can be saved at the current exterior. */
    private boolean canCreateNew() {
        return canSave && !hasWaypointAtCurrentLocation();
    }

    private boolean hasWaypointAtCurrentLocation() {
        if (locationWaypointId != null) {
            return true;
        }
        return findWaypointIdAtExterior() != null;
    }

    private @Nullable UUID findWaypointIdAtExterior() {
        if (exteriorLocation == null) {
            return null;
        }
        for (OpenWaypointScreen.WaypointEntry entry : waypoints) {
            if (entry == null || entry.dimension() == null) {
                continue;
            }
            if (exteriorLocation.dimension().equals(entry.dimension())
                    && exteriorLocation.x() == entry.x()
                    && exteriorLocation.y() == entry.y()
                    && exteriorLocation.z() == entry.z()) {
                return entry.id();
            }
        }
        return null;
    }

    private Component newButtonTooltip() {
        if (hasWaypointAtCurrentLocation()) {
            return Component.translatable("dwm.gui.waypoint.tooltip.new_exists");
        }
        return Component.translatable("dwm.gui.waypoint.tooltip.new");
    }

    private void refreshNewButton() {
        if (newButton == null) {
            return;
        }
        newButton.active = canCreateNew();
        newButton.setTooltip(Tooltip.create(newButtonTooltip()));
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

        deleteButton = destructiveIconButton(
                Component.translatable("dwm.gui.waypoint.delete"),
                Component.translatable("dwm.gui.waypoint.tooltip.delete"),
                ICON_DELETE,
                button -> {
                    OpenWaypointScreen.WaypointEntry selected = selectedWaypoint();
                    if (selected != null) {
                        confirmDelete(selected);
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
                newButtonTooltip(),
                ICON_NEW,
                button -> beginCreate()
        );
        newButton.setPosition(panelLeft + 10, footerY);
        refreshNewButton();

        doneButton = Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 38, footerY, PANEL_WIDTH - 48, 20)
                .build();

        nameField = new EditBox(
                font,
                detailLeft + 8,
                detailTop + 24,
                DETAIL_WIDTH - 16,
                20,
                Component.translatable("dwm.gui.waypoint.name")
        );
        nameField.setMaxLength(MAX_NAME_LENGTH);
        nameField.setHint(Component.translatable("dwm.gui.waypoint.name"));
        nameField.setResponder(value -> updateConfirmActive());

        confirmButton = Button.builder(Component.translatable("dwm.gui.waypoint.save"), button -> confirmName())
                .bounds(detailLeft + 4, detailButtonY, DETAIL_BUTTON_WIDTH, 20)
                .build();

        cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> {
            if (!createPending) {
                exitNameMode();
            }
        })
                .bounds(detailLeft + DETAIL_WIDTH - DETAIL_BUTTON_WIDTH - 4, detailButtonY, DETAIL_BUTTON_WIDTH, 20)
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
        updateConfirmActive();
        if (waypoints.isEmpty() && canCreateNew()) {
            beginCreate();
        }
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

    private static SpriteIconButton destructiveIconButton(
            Component message,
            Component tooltip,
            Identifier sprite,
            Button.OnPress onPress
    ) {
        return new DestructiveIconButton(
                ACTION_BUTTON_SIZE,
                ACTION_BUTTON_SIZE,
                message,
                ACTION_SPRITE_SIZE,
                ACTION_SPRITE_SIZE,
                0,
                0,
                new WidgetSprites(sprite),
                onPress,
                tooltip,
                null,
                false
        );
    }

    private void confirmDelete(OpenWaypointScreen.WaypointEntry entry) {
        minecraft.gui.setScreen(
                new PopupScreen.Builder(
                        this,
                        Component.translatable("dwm.gui.waypoint.delete.confirm.title")
                )
                        .addMessage(Component.translatable(
                                "dwm.gui.waypoint.delete.confirm.message",
                                entry.name()
                        ))
                        .addButton(CommonComponents.GUI_YES, popup -> {
                            deleteWaypoint(entry);
                            popup.onClose();
                        })
                        .addButton(CommonComponents.GUI_NO, PopupScreen::onClose)
                        .build()
        );
    }

    private void rebuildListEntries() {
        UUID keepId = selectedId;
        list.clearAll();
        WaypointList.WaypointEntryRow toSelect = null;

        WaypointList.WaypointEntryRow ghost = list.new WaypointEntryRow(ghostEntry());
        list.addRow(ghost);
        if (nameMode == NameMode.CREATE) {
            toSelect = ghost;
            keepId = GHOST_ROW_ID;
        }

        if (waypoints.isEmpty()) {
            list.addRow(list.new WaypointEntryRow(emptyHintEntry()));
        }

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
        } else {
            WaypointList.WaypointEntryRow firstReal = firstRealRow();
            if (firstReal != null) {
                list.setSelected(firstReal);
                selectedId = firstReal.waypoint.id();
            } else {
                list.setSelected(null);
                selectedId = null;
            }
        }
        updateDetailActions();
    }

    private @Nullable WaypointList.WaypointEntryRow firstRealRow() {
        for (WaypointList.WaypointEntryRow row : list.children()) {
            UUID id = row.waypoint.id();
            if (!isGhost(id) && !isEmptyHint(id)) {
                return row;
            }
        }
        return null;
    }

    private static OpenWaypointScreen.WaypointEntry ghostEntry() {
        return new OpenWaypointScreen.WaypointEntry(GHOST_ROW_ID, "", "", 0, 0, 0, 0);
    }

    private static OpenWaypointScreen.WaypointEntry emptyHintEntry() {
        return new OpenWaypointScreen.WaypointEntry(EMPTY_HINT_ROW_ID, "", "", 0, 0, 0, 0);
    }

    private static boolean isGhost(@Nullable UUID id) {
        return GHOST_ROW_ID.equals(id);
    }

    private static boolean isEmptyHint(@Nullable UUID id) {
        return EMPTY_HINT_ROW_ID.equals(id);
    }

    private @Nullable OpenWaypointScreen.WaypointEntry selectedWaypoint() {
        if (selectedId == null || isGhost(selectedId) || isEmptyHint(selectedId)) {
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

    private void updateConfirmActive() {
        if (confirmButton == null || nameField == null) {
            return;
        }
        String name = nameField.getValue() == null ? "" : nameField.getValue().trim();
        confirmButton.active = !name.isEmpty() && !createPending;
    }

    private void beginCreate() {
        if (!canCreateNew() || createPending) {
            return;
        }
        nameMode = NameMode.CREATE;
        renameTargetId = null;
        nameField.setValue("");
        confirmButton.setMessage(Component.translatable("dwm.gui.waypoint.save"));
        selectedId = GHOST_ROW_ID;
        rebuildListEntries();
        applyModeVisibility();
        updateConfirmActive();
        setInitialFocus(nameField);
    }

    private void beginRename(OpenWaypointScreen.WaypointEntry entry) {
        if (createPending) {
            return;
        }
        nameMode = NameMode.RENAME;
        renameTargetId = entry.id();
        nameField.setValue(entry.name() == null ? "" : entry.name());
        confirmButton.setMessage(Component.translatable("dwm.gui.waypoint.rename"));
        selectedId = entry.id();
        rebuildListEntries();
        applyModeVisibility();
        updateConfirmActive();
        setInitialFocus(nameField);
    }

    private void exitNameMode() {
        if (createPending) {
            return;
        }
        nameMode = NameMode.LIST;
        renameTargetId = null;
        nameField.setValue("");
        if (isGhost(selectedId)) {
            selectedId = preferSelectedId();
        }
        rebuildListEntries();
        applyModeVisibility();
        updateDetailActions();
        updateConfirmActive();
    }

    private void confirmName() {
        String name = nameField.getValue() == null ? "" : nameField.getValue().trim();
        if (name.isEmpty() || createPending) {
            return;
        }
        if (nameMode == NameMode.CREATE) {
            createPending = true;
            updateConfirmActive();
            cancelButton.active = false;
            nameField.setEditable(false);
            tardis.saveWaypoint(name);
            // Stay in create mode until OpenWaypointScreen S2C replaces this screen.
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
        if (entry.id().equals(locationWaypointId)) {
            locationWaypointId = null;
        }
        rebuildListEntries();
        refreshNewButton();
        if (nameMode == NameMode.RENAME && entry.id().equals(renameTargetId)) {
            exitNameMode();
        }
        if (waypoints.isEmpty() && canCreateNew()) {
            beginCreate();
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
        list.visible = true;
        list.active = !naming;
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
            nameField.setEditable(!createPending);
        }
        updateDetailActions();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (nameMode != NameMode.LIST && event.isEscape()) {
            if (!createPending) {
                exitNameMode();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x1 = width / 2 - PANEL_WIDTH / 2;
        int y1 = height / 2 - PANEL_HEIGHT / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x1, y1, PANEL_WIDTH, PANEL_HEIGHT);

        // Draw behind widgets so action buttons stay clickable and visible.
        graphics.fill(detailLeft, detailTop, detailLeft + DETAIL_WIDTH, detailTop + BODY_HEIGHT, 0xFF333333);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(font, getTitle(), x1 + 10, y1 + 10, 0xFFFFFFFF, false);

        if (nameMode != NameMode.LIST) {
            renderNameModeDetail(graphics);
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
        graphics.text(font, DimensionNames.of(selected.dimension()), textX, detailTop + 56, 0xFFFFFFFF, false);
        String coords = selected.x() + ", " + selected.y() + ", " + selected.z();
        graphics.text(font, Component.literal(coords), textX, detailTop + 68, 0xFFA0A0A0, false);
    }

    private void renderNameModeDetail(GuiGraphicsExtractor graphics) {
        int textX = detailLeft + 8;
        Component modeLabel = nameMode == NameMode.CREATE
                ? Component.translatable("dwm.gui.waypoint.new")
                : Component.translatable("dwm.gui.waypoint.rename.title");
        graphics.text(font, modeLabel, textX, detailTop + 8, MODE_LABEL_COLOR, false);

        graphics.text(font, Component.translatable("dwm.gui.waypoint.detail.location"), textX, detailTop + 52, 0xFF888888, false);

        String dimension;
        String coords;
        if (nameMode == NameMode.CREATE) {
            if (exteriorLocation != null) {
                dimension = exteriorLocation.dimension();
                coords = exteriorLocation.x() + ", " + exteriorLocation.y() + ", " + exteriorLocation.z();
            } else {
                dimension = "";
                coords = "";
            }
        } else {
            OpenWaypointScreen.WaypointEntry selected = null;
            if (renameTargetId != null) {
                for (OpenWaypointScreen.WaypointEntry entry : waypoints) {
                    if (renameTargetId.equals(entry.id())) {
                        selected = entry;
                        break;
                    }
                }
            }
            if (selected == null) {
                selected = selectedWaypoint();
            }
            if (selected != null) {
                dimension = selected.dimension();
                coords = selected.x() + ", " + selected.y() + ", " + selected.z();
            } else {
                dimension = "";
                coords = "";
            }
        }

        if (!dimension.isEmpty()) {
            graphics.text(font, DimensionNames.of(dimension), textX, detailTop + 64, 0xFFFFFFFF, false);
            graphics.text(font, Component.literal(coords), textX, detailTop + 76, 0xFFA0A0A0, false);
        }
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

    /**
     * Icon-only button that tints its sprite red to signal a destructive action.
     */
    private static final class DestructiveIconButton extends SpriteIconButton.CenteredIcon {
        private DestructiveIconButton(
                int width,
                int height,
                Component message,
                int spriteWidth,
                int spriteHeight,
                int spriteOffsetX,
                int spriteOffsetY,
                WidgetSprites sprite,
                Button.OnPress onPress,
                @Nullable Component tooltip,
                Button.@Nullable CreateNarration narration,
                boolean switchToLoadingAfterPress
        ) {
            super(
                    width,
                    height,
                    message,
                    spriteWidth,
                    spriteHeight,
                    spriteOffsetX,
                    spriteOffsetY,
                    sprite,
                    onPress,
                    tooltip,
                    narration,
                    switchToLoadingAfterPress
            );
        }

        @Override
        protected void extractSprite(GuiGraphicsExtractor graphics, int x, int y) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    this.sprite.get(this.isActive(), this.isHoveredOrFocused()),
                    x,
                    y,
                    this.spriteWidth,
                    this.spriteHeight,
                    ARGB.colorFromFloat(this.alpha, 1.0F, 0.35F, 0.35F)
            );
        }
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
            if (entry != null && isGhost(entry.waypoint.id()) && nameMode != NameMode.CREATE && !canCreateNew()) {
                return;
            }
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
                if (isEmptyHint(waypoint.id())) {
                    graphics.text(
                            font,
                            Component.translatable("dwm.gui.waypoint.empty"),
                            getContentX() + 4,
                            getContentYMiddle() - 4,
                            EMPTY_HINT_TEXT_COLOR,
                            false
                    );
                    return;
                }

                boolean ghost = isGhost(waypoint.id());
                if (ghost) {
                    int textColor;
                    if (!canCreateNew()) {
                        textColor = GHOST_TEXT_COLOR_DISABLED;
                    } else if (isFocused() || WaypointList.this.getSelected() == this) {
                        textColor = GHOST_TEXT_COLOR;
                    } else {
                        textColor = GHOST_TEXT_COLOR_MUTED;
                    }
                    graphics.text(
                            font,
                            Component.translatable("dwm.gui.waypoint.new.ghost"),
                            getContentX() + 4,
                            getContentYMiddle() - 4,
                            textColor,
                            false
                    );
                    if (hovered && !canCreateNew()) {
                        graphics.setTooltipForNextFrame(newButtonTooltip(), mouseX, mouseY);
                    }
                    return;
                }

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
                if (isEmptyHint(waypoint.id())) {
                    return Component.translatable("dwm.gui.waypoint.empty");
                }
                if (isGhost(waypoint.id())) {
                    if (!canCreateNew()) {
                        return newButtonTooltip();
                    }
                    return Component.translatable("dwm.gui.waypoint.new.ghost");
                }
                return Component.literal(waypoint.name());
            }

            @Override
            public boolean shouldTakeFocusAfterInteraction() {
                if (isGhost(waypoint.id()) && !canCreateNew()) {
                    return false;
                }
                return super.shouldTakeFocusAfterInteraction();
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (nameMode != NameMode.LIST) {
                    return false;
                }
                if (isEmptyHint(waypoint.id())) {
                    return false;
                }
                if (isGhost(waypoint.id())) {
                    if (!canCreateNew() || createPending) {
                        return false;
                    }
                    beginCreate();
                    return true;
                }
                WaypointList.this.setSelected(this);
                return true;
            }
        }
    }
}
