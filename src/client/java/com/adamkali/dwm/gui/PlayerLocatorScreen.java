package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.network.OpenPlayerLocatorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
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
public class PlayerLocatorScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");
    private static final Identifier ICON_SELECTED =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/selected");
    private static final Identifier ICON_CLEAR =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "waypoint/clear");
    /** Client-only sentinel for the empty-list hint row. */
    private static final UUID EMPTY_HINT_ROW_ID = new UUID(0L, 1L);
    private static final int EMPTY_HINT_TEXT_COLOR = 0xFF888888;
    private static final int ROW_HEIGHT = 22;
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 200;
    private static final int LIST_WIDTH = 136;
    private static final int DETAIL_WIDTH = 136;
    private static final int BODY_HEIGHT = 118;
    private static final int ICON_SIZE = 10;
    private static final int ACTION_BUTTON_SIZE = 20;
    private static final int ACTION_SPRITE_SIZE = 12;

    private final ClientTardis tardis;
    private final List<OpenPlayerLocatorScreen.PlayerEntry> players;
    private @Nullable UUID selectedPlayerUuid;
    private @Nullable UUID selectedId;

    private PlayerList list;
    private SpriteIconButton selectDestinationButton;
    private SpriteIconButton clearDestinationButton;
    private Button doneButton;

    private int panelLeft;
    private int panelTop;
    private int detailLeft;
    private int detailTop;

    public PlayerLocatorScreen(
            ClientTardis tardis,
            List<OpenPlayerLocatorScreen.PlayerEntry> players,
            @Nullable UUID selectedPlayerUuid
    ) {
        super(Component.translatable("dwm.gui.player_locator.title"));
        this.tardis = tardis;
        this.players = new ArrayList<>(players);
        this.selectedPlayerUuid = selectedPlayerUuid;
        this.selectedId = preferSelectedId();
    }

    private @Nullable UUID preferSelectedId() {
        if (containsPlayer(selectedPlayerUuid)) {
            return selectedPlayerUuid;
        }
        if (!players.isEmpty()) {
            return players.getFirst().uuid();
        }
        return null;
    }

    private boolean containsPlayer(@Nullable UUID id) {
        if (id == null) {
            return false;
        }
        for (OpenPlayerLocatorScreen.PlayerEntry entry : players) {
            if (id.equals(entry.uuid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void init() {
        panelLeft = width / 2 - PANEL_WIDTH / 2;
        panelTop = height / 2 - PANEL_HEIGHT / 2;
        int bodyTop = panelTop + 28;
        detailLeft = panelLeft + 10 + LIST_WIDTH + 6;
        detailTop = bodyTop;

        int detailButtonY = detailTop + BODY_HEIGHT - 26;

        selectDestinationButton = iconButton(
                Component.translatable("dwm.gui.player_locator.select"),
                Component.translatable("dwm.gui.player_locator.tooltip.select"),
                ICON_SELECTED,
                button -> selectCurrent()
        );
        selectDestinationButton.setPosition(detailLeft + DETAIL_WIDTH - ACTION_BUTTON_SIZE - 4, detailButtonY);

        clearDestinationButton = iconButton(
                Component.translatable("dwm.gui.player_locator.clear"),
                Component.translatable("dwm.gui.player_locator.tooltip.clear"),
                ICON_CLEAR,
                button -> clearDestination()
        );
        clearDestinationButton.setPosition(detailLeft + DETAIL_WIDTH - ACTION_BUTTON_SIZE - 4, detailButtonY);

        int footerY = panelTop + PANEL_HEIGHT - 40;
        doneButton = Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 10, footerY, PANEL_WIDTH - 20, 20)
                .build();

        list = new PlayerList(minecraft, LIST_WIDTH, BODY_HEIGHT, bodyTop, ROW_HEIGHT);
        list.updateSizeAndPosition(LIST_WIDTH, BODY_HEIGHT, panelLeft + 10, bodyTop);
        rebuildListEntries();

        addRenderableWidget(list);
        addRenderableWidget(selectDestinationButton);
        addRenderableWidget(clearDestinationButton);
        addRenderableWidget(doneButton);

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
        PlayerList.PlayerEntryRow toSelect = null;

        if (players.isEmpty()) {
            list.addRow(list.new PlayerEntryRow(emptyHintEntry()));
        }

        for (OpenPlayerLocatorScreen.PlayerEntry entry : players) {
            PlayerList.PlayerEntryRow row = list.new PlayerEntryRow(entry);
            list.addRow(row);
            if (keepId != null && keepId.equals(entry.uuid())) {
                toSelect = row;
            }
        }

        if (toSelect != null) {
            list.setSelected(toSelect);
            selectedId = toSelect.player.uuid();
        } else {
            PlayerList.PlayerEntryRow firstReal = firstRealRow();
            if (firstReal != null) {
                list.setSelected(firstReal);
                selectedId = firstReal.player.uuid();
            } else {
                list.setSelected(null);
                selectedId = null;
            }
        }
        updateDetailActions();
    }

    private @Nullable PlayerList.PlayerEntryRow firstRealRow() {
        for (PlayerList.PlayerEntryRow row : list.children()) {
            if (!isEmptyHint(row.player.uuid())) {
                return row;
            }
        }
        return null;
    }

    private static OpenPlayerLocatorScreen.PlayerEntry emptyHintEntry() {
        return new OpenPlayerLocatorScreen.PlayerEntry(EMPTY_HINT_ROW_ID, "", "", 0, 0, 0);
    }

    private static boolean isEmptyHint(@Nullable UUID id) {
        return EMPTY_HINT_ROW_ID.equals(id);
    }

    private @Nullable OpenPlayerLocatorScreen.PlayerEntry selectedPlayer() {
        if (selectedId == null || isEmptyHint(selectedId)) {
            return null;
        }
        for (OpenPlayerLocatorScreen.PlayerEntry entry : players) {
            if (selectedId.equals(entry.uuid())) {
                return entry;
            }
        }
        return null;
    }

    private void onRowSelected(OpenPlayerLocatorScreen.PlayerEntry entry) {
        selectedId = entry.uuid();
        updateDetailActions();
    }

    private void updateDetailActions() {
        if (selectDestinationButton == null || clearDestinationButton == null) {
            return;
        }
        boolean hasSelection = selectedPlayer() != null;
        boolean isDestination = hasSelection
                && selectedPlayerUuid != null
                && selectedPlayerUuid.equals(selectedId);

        selectDestinationButton.active = hasSelection && !isDestination;
        clearDestinationButton.active = hasSelection && isDestination;

        selectDestinationButton.visible = hasSelection && !isDestination;
        clearDestinationButton.visible = hasSelection && isDestination;
    }

    private void selectCurrent() {
        OpenPlayerLocatorScreen.PlayerEntry selected = selectedPlayer();
        if (selected == null) {
            return;
        }
        tardis.selectPlayer(selected.uuid());
        selectedPlayerUuid = selected.uuid();
        updateDetailActions();
    }

    private void clearDestination() {
        tardis.selectPlayer(null);
        selectedPlayerUuid = null;
        updateDetailActions();
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

        OpenPlayerLocatorScreen.PlayerEntry selected = selectedPlayer();
        if (selected == null) {
            return;
        }

        int textX = detailLeft + 8;
        graphics.text(font, Component.translatable("dwm.gui.player_locator.detail.name"), textX, detailTop + 8, 0xFF888888, false);
        graphics.text(font, Component.literal(selected.name()), textX, detailTop + 20, 0xFFFFFFFF, false);

        graphics.text(font, Component.translatable("dwm.gui.player_locator.detail.location"), textX, detailTop + 44, 0xFF888888, false);
        graphics.text(font, Component.literal(selected.dimension()), textX, detailTop + 56, 0xFFFFFFFF, false);
        String coords = selected.x() + ", " + selected.y() + ", " + selected.z();
        graphics.text(font, Component.literal(coords), textX, detailTop + 68, 0xFFA0A0A0, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class PlayerList extends ObjectSelectionList<PlayerList.PlayerEntryRow> {
        PlayerList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        void clearAll() {
            clearEntries();
        }

        int addRow(PlayerEntryRow entry) {
            return addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH - 8;
        }

        @Override
        public void setSelected(@Nullable PlayerEntryRow entry) {
            if (entry != null && isEmptyHint(entry.player.uuid())) {
                return;
            }
            super.setSelected(entry);
            if (entry != null) {
                onRowSelected(entry.player);
            }
        }

        class PlayerEntryRow extends ObjectSelectionList.Entry<PlayerEntryRow> {
            private final OpenPlayerLocatorScreen.PlayerEntry player;

            PlayerEntryRow(OpenPlayerLocatorScreen.PlayerEntry player) {
                this.player = player;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
                if (isEmptyHint(player.uuid())) {
                    graphics.text(
                            font,
                            Component.translatable("dwm.gui.player_locator.empty"),
                            getContentX() + 4,
                            getContentYMiddle() - 4,
                            EMPTY_HINT_TEXT_COLOR,
                            false
                    );
                    return;
                }

                boolean isDestination = selectedPlayerUuid != null && selectedPlayerUuid.equals(player.uuid());
                int textColor = isFocused() || PlayerList.this.getSelected() == this ? 0xFFFFFFFF : 0xFFE0E0E0;
                graphics.text(
                        font,
                        Component.literal(player.name()),
                        getContentX() + 4,
                        getContentYMiddle() - 4,
                        textColor,
                        false
                );

                if (!isDestination) {
                    return;
                }
                int iconX = getContentRight() - ICON_SIZE - 4;
                int iconY = getContentYMiddle() - ICON_SIZE / 2;
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
                            Component.translatable("dwm.gui.player_locator.tooltip.destination"),
                            mouseX,
                            mouseY
                    );
                }
            }

            private static boolean isMouseOverIcon(int mouseX, int mouseY, int iconX, int iconY) {
                return mouseX >= iconX && mouseX < iconX + ICON_SIZE
                        && mouseY >= iconY && mouseY < iconY + ICON_SIZE;
            }

            @Override
            public Component getNarration() {
                if (isEmptyHint(player.uuid())) {
                    return Component.translatable("dwm.gui.player_locator.empty");
                }
                return Component.literal(player.name());
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (isEmptyHint(player.uuid())) {
                    return false;
                }
                PlayerList.this.setSelected(this);
                return true;
            }
        }
    }
}
