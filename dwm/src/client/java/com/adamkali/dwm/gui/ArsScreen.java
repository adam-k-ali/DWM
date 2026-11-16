package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.network.OpenArsScreen;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsConnector;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceDefinition;
import com.adamkali.dwm.tardis.interior.ars.ArsRegistries;
import com.adamkali.dwm.tardis.interior.ars.ArsService;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Architectural Reconfiguration System map, opened from the console's ARS control. The left half is the
 * plot grid as a north-up floor plan; the right half is the piece palette and a readout of the selection.
 * Growing and jettisoning are requests to the server, which re-sends {@link OpenArsScreen} so this screen
 * {@link #update updates in place}. Sized for a 427×240 scaled GUI.
 */
@Environment(EnvType.CLIENT)
public class ArsScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");

    private static final int PANEL_WIDTH = 404;
    private static final int PANEL_HEIGHT = 228;
    private static final int MARGIN = 10;
    private static final int CELL = 20;
    private static final int MAP_TOP = 22;
    private static final int SIDE_LEFT = MARGIN + CELL * ArsGrid.GRID_SIZE + 10;
    private static final int SIDE_WIDTH = PANEL_WIDTH - SIDE_LEFT - MARGIN;
    private static final int PALETTE_ROW = 11;
    private static final int PALETTE_ROWS_TOP = MAP_TOP + 11;
    /** Rows that fit above the detail box; a datapack with more pieces scrolls the palette. */
    private static final int PALETTE_VISIBLE_ROWS = 8;
    private static final int DETAIL_TOP = 124;
    private static final int DETAIL_BOTTOM = 184;
    private static final int HINT_Y = 189;
    private static final int BUTTON_Y = 202;
    private static final int BUTTON_WIDTH = 80;
    private static final int SWATCH = 7;
    /** Connector aperture along a cell edge: block offsets 2..5 of 8. */
    private static final int APERTURE_FROM = CELL * ArsGrid.APERTURE_MIN_OFFSET / ArsGrid.CELL_SIZE;
    private static final int APERTURE_TO = CELL * (ArsGrid.APERTURE_MAX_OFFSET + 1) / ArsGrid.CELL_SIZE;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int LABEL = 0xFF9A9A9A;
    private static final int GOOD = 0xFF7FE07F;
    private static final int BAD = 0xFFFF7A6A;
    private static final int BOX = 0xFF333333;
    private static final int MAP_BORDER = 0xFF0C0E10;
    private static final int MAP_BACKGROUND = 0xFF1C2127;
    private static final int GRID_LINE = 0xFF2B323A;
    private static final int CONSOLE_FILL = 0xFF39505C;
    private static final int CONSOLE_EDGE = 0xFF6F93A3;
    private static final int CONSOLE_MARK = 0xFFE8F4FF;
    private static final int ROOT_MARK = 0xFFE0A030;
    /** Police-box blue for the TARDIS's main doors. */
    private static final int ENTRANCE_FILL = 0xFF2F63B8;
    private static final int ENTRANCE_EDGE = 0xFFB9D4FF;
    private static final int PIECE_EDGE = 0xFF0A0A0A;
    private static final int DOOR = 0xFFF4F1E6;
    private static final int SELECTED_EDGE = 0xFFFFE070;
    private static final int HOVER_EDGE = 0x90FFFFFF;
    private static final int GHOST_OK = 0x6040E040;
    private static final int GHOST_BAD = 0x70E04040;
    private static final int ROW_SELECTED = 0xFF4A5C70;
    private static final int ROW_HOVER = 0xFF404850;

    private final ClientTardis tardis;
    private final ArsMapState state = new ArsMapState();
    private Map<Identifier, ArsPieceDefinition> definitions = Map.of();
    private Function<Identifier, ArsPieceDefinition> lookup = id -> null;
    private List<Identifier> palette = List.of();
    private int paletteScroll;

    private Button rotateButton;
    private Button jettisonButton;
    private int panelLeft;
    private int panelTop;

    public ArsScreen(ClientTardis tardis, OpenArsScreen payload) {
        super(Component.translatable("dwm.gui.ars.title"));
        this.tardis = tardis;
        update(payload);
    }

    public UUID tardisId() {
        return tardis.getTardisId();
    }

    /** Selection, palette and ghost state (for tests). */
    public ArsMapState state() {
        return state;
    }

    /** Applies a fresh server snapshot without closing the screen; palette selection and rotation survive. */
    public void update(OpenArsScreen payload) {
        loadDefinitions();
        state.update(payload.placedPieces(), payload.artron(), lookup);
        updateButtons();
    }

    private void loadDefinitions() {
        Minecraft client = Minecraft.getInstance();
        Map<Identifier, ArsPieceDefinition> loaded = new HashMap<>();
        if (client.level != null) {
            Optional<Registry<ArsPieceDefinition>> registry = client.level.registryAccess().lookup(ArsRegistries.PIECE);
            if (registry.isPresent()) {
                for (Identifier id : registry.get().keySet()) {
                    ArsPieceDefinition definition = registry.get().getValue(id);
                    if (definition != null) {
                        loaded.put(id, definition);
                    }
                }
            }
        }
        definitions = Map.copyOf(loaded);
        lookup = definitions::get;
        palette = ArsMapState.paletteOrder(definitions);
        paletteScroll = Math.clamp(paletteScroll, 0, Math.max(0, palette.size() - PALETTE_VISIBLE_ROWS));
    }

    @Override
    protected void init() {
        panelLeft = width / 2 - PANEL_WIDTH / 2;
        panelTop = height / 2 - PANEL_HEIGHT / 2;
        int buttonY = panelTop + BUTTON_Y;
        rotateButton = addRenderableWidget(Button.builder(Component.translatable("dwm.gui.ars.rotate"), button -> rotate())
                .bounds(panelLeft + MARGIN, buttonY, BUTTON_WIDTH, 20)
                .tooltip(Tooltip.create(Component.translatable("dwm.gui.ars.tooltip.rotate")))
                .build());
        jettisonButton = addRenderableWidget(Button.builder(Component.translatable("dwm.gui.ars.jettison"), button -> jettison())
                .bounds(panelLeft + MARGIN + BUTTON_WIDTH + 4, buttonY, BUTTON_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + PANEL_WIDTH - MARGIN - BUTTON_WIDTH, buttonY, BUTTON_WIDTH, 20)
                .build());
        updateButtons();
    }

    /** Map placement for the current screen size. */
    public ArsMapState.MapGeometry geometry() {
        return new ArsMapState.MapGeometry(panelLeft + MARGIN, panelTop + MAP_TOP, CELL);
    }

    /** GUI-space centre of a map cell. */
    public int[] cellCenter(ArsCell cell) {
        ArsMapState.MapGeometry geometry = geometry();
        return new int[]{geometry.cellLeft(cell.x()) + CELL / 2, geometry.cellTop(cell.z()) + CELL / 2};
    }

    /** GUI-space centre of a palette row, or {@code null} if the piece is not in the palette. */
    public @Nullable int[] paletteRowCenter(Identifier pieceId) {
        int index = palette.indexOf(pieceId) - paletteScroll;
        if (index < 0 || index >= PALETTE_VISIBLE_ROWS) {
            return null;
        }
        return new int[]{panelLeft + SIDE_LEFT + SIDE_WIDTH / 2, panelTop + PALETTE_ROWS_TOP + index * PALETTE_ROW + PALETTE_ROW / 2};
    }

    /** The rules' answer for the selected palette piece anchored at {@code anchor}, or null with none selected. */
    public ArsLayoutLogic.@Nullable PlaceResult placementAt(ArsCell anchor) {
        return state.ghostAt(anchor, lookup).map(ghost -> state.check(ghost, lookup, instabuild())).orElse(null);
    }

    private boolean instabuild() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.player.getAbilities().instabuild;
    }

    private void rotate() {
        state.rotateClockwise();
    }

    private void jettison() {
        Optional<ArsPlacedPiece> selected = state.selectedPlaced(lookup);
        if (selected.isEmpty() || state.removeCheck(lookup) != ArsLayoutLogic.RemoveResult.OK) {
            return;
        }
        ArsCell cell = state.selectedCell();
        tardis.arsJettison(cell.x(), cell.z());
    }

    private void updateButtons() {
        if (jettisonButton == null) {
            return;
        }
        ArsLayoutLogic.RemoveResult removal = state.removeCheck(lookup);
        boolean hasSelection = state.selectedPlaced(lookup).isPresent();
        jettisonButton.active = hasSelection && removal == ArsLayoutLogic.RemoveResult.OK;
        if (!hasSelection) {
            jettisonButton.setTooltip(Tooltip.create(Component.translatable("dwm.gui.ars.tooltip.jettison_select")));
        } else if (removal != ArsLayoutLogic.RemoveResult.OK) {
            jettisonButton.setTooltip(Tooltip.create(Component.translatable(removal.translationKey())));
        } else {
            jettisonButton.setTooltip(Tooltip.create(Component.translatable("dwm.gui.ars.tooltip.jettison")));
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_R) {
            rotate();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Identifier row = paletteRowAt(event.x(), event.y());
            if (row != null) {
                state.togglePalette(row);
                playClick();
                updateButtons();
                return true;
            }
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return false;
        }
        ArsCell cell = geometry().cellAt(event.x(), event.y());
        if (cell == null) {
            return false;
        }
        ArsMapState.Click click = state.click(cell, button == GLFW.GLFW_MOUSE_BUTTON_RIGHT, lookup, instabuild());
        switch (click.kind()) {
            case GROW -> {
                ArsPlacedPiece grow = click.grow();
                tardis.arsGrow(Identifier.parse(grow.pieceId), grow.cellX, grow.cellZ, grow.rotation);
                playClick();
            }
            case SELECTED, DESELECTED -> playClick();
            case REFUSED, NONE -> {
            }
        }
        updateButtons();
        return click.kind() != ArsMapState.ClickKind.NONE;
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private @Nullable Identifier paletteRowAt(double mouseX, double mouseY) {
        int x0 = panelLeft + SIDE_LEFT;
        int y0 = panelTop + PALETTE_ROWS_TOP;
        if (mouseX < x0 || mouseX >= x0 + SIDE_WIDTH || mouseY < y0) {
            return null;
        }
        int row = (int) Math.floor((mouseY - y0) / PALETTE_ROW);
        int index = row + paletteScroll;
        return row < PALETTE_VISIBLE_ROWS && index < palette.size() ? palette.get(index) : null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x0 = panelLeft + SIDE_LEFT;
        int y0 = panelTop + PALETTE_ROWS_TOP;
        if (mouseX >= x0 && mouseX < x0 + SIDE_WIDTH && mouseY >= y0 && mouseY < y0 + PALETTE_VISIBLE_ROWS * PALETTE_ROW
                && palette.size() > PALETTE_VISIBLE_ROWS) {
            int max = palette.size() - PALETTE_VISIBLE_ROWS;
            paletteScroll = Math.clamp(paletteScroll - (long) Math.signum(scrollY), 0, max);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        int sideX = panelLeft + SIDE_LEFT;
        graphics.fill(sideX, panelTop + MAP_TOP, sideX + SIDE_WIDTH,
                panelTop + PALETTE_ROWS_TOP + PALETTE_VISIBLE_ROWS * PALETTE_ROW + 2, BOX);
        graphics.fill(sideX, panelTop + DETAIL_TOP, sideX + SIDE_WIDTH, panelTop + DETAIL_BOTTOM, BOX);

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(font, getTitle(), panelLeft + MARGIN, panelTop + 9, WHITE, false);
        Component north = Component.translatable("dwm.gui.ars.north");
        graphics.text(font, north, panelLeft + PANEL_WIDTH - MARGIN - font.width(north), panelTop + 9, LABEL, false);

        ArsCell hovered = geometry().cellAt(mouseX, mouseY);
        drawMap(graphics, hovered, mouseX, mouseY);
        drawPalette(graphics, mouseX, mouseY);
        drawDetail(graphics, hovered);
        graphics.text(font, Component.translatable("dwm.gui.ars.hint"), panelLeft + MARGIN, panelTop + HINT_Y, LABEL, false);
    }

    // ---- map -------------------------------------------------------------------------------------

    private void drawMap(GuiGraphicsExtractor graphics, @Nullable ArsCell hovered, int mouseX, int mouseY) {
        ArsMapState.MapGeometry geo = geometry();
        int left = geo.left();
        int top = geo.top();
        int size = geo.size();
        graphics.fill(left - 1, top - 1, left + size + 1, top + size + 1, MAP_BORDER);
        graphics.fill(left, top, left + size, top + size, MAP_BACKGROUND);
        for (int i = 1; i < ArsGrid.GRID_SIZE; i++) {
            graphics.fill(left + i * CELL, top, left + i * CELL + 1, top + size, GRID_LINE);
            graphics.fill(left, top + i * CELL, left + size, top + i * CELL + 1, GRID_LINE);
        }

        drawConsoleRoom(graphics, geo);

        List<ArsPlacedPiece> layout = state.layout();
        Set<ArsLayoutLogic.Link> links = ArsLayoutLogic.openLinks(layout, lookup);
        for (ArsPlacedPiece piece : layout) {
            ArsPieceDefinition definition = ArsLayoutLogic.definitionOf(piece, lookup);
            if (definition == null || !ArsGrid.isValidRotation(piece.rotation)) {
                continue;
            }
            List<ArsCell> cells = ArsLayoutLogic.occupiedCells(piece, definition);
            int color = 0xFF000000 | definition.mapColor();
            for (ArsCell cell : cells) {
                fillCell(graphics, geo, cell, color);
            }
            outline(graphics, geo, new HashSet<>(cells), PIECE_EDGE, 1);
            for (ArsConnector connector : ArsLayoutLogic.connectors(piece, definition)) {
                if (!links.contains(ArsLayoutLogic.Link.of(connector))) {
                    notch(graphics, geo, connector, darker(color));
                }
            }
        }

        for (ArsLayoutLogic.Link link : links) {
            doorway(graphics, geo, link, DOOR);
        }
        // Each console doorway (east and west) is amber until a piece opens it.
        for (ArsLayoutLogic.Link root : ArsLayoutLogic.ROOT_LINKS) {
            if (!links.contains(root)) {
                doorway(graphics, geo, root, ROOT_MARK);
            }
        }

        state.selectedPlaced(lookup).ifPresent(piece -> {
            ArsPieceDefinition definition = ArsLayoutLogic.definitionOf(piece, lookup);
            if (definition != null) {
                outline(graphics, geo, new HashSet<>(ArsLayoutLogic.occupiedCells(piece, definition)), SELECTED_EDGE, 2);
            }
        });

        Optional<ArsMapState.Ghost> ghost = state.ghostAt(hovered, lookup);
        if (ghost.isPresent()) {
            drawGhost(graphics, geo, ghost.get());
        } else if (hovered != null) {
            outline(graphics, geo, Set.of(hovered), HOVER_EDGE, 1);
        }

        if (isNearRootDoorway(geo, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("dwm.gui.ars.console_doorway"), mouseX, mouseY);
        } else if (geo.entranceRect().contains(mouseX, mouseY, 2)) {
            graphics.setTooltipForNextFrame(Component.translatable("dwm.gui.ars.entrance_tooltip"), mouseX, mouseY);
        }
    }

    private void drawConsoleRoom(GuiGraphicsExtractor graphics, ArsMapState.MapGeometry geo) {
        int x0 = geo.cellLeft(ArsGrid.RESERVED_MIN_X);
        int z0 = geo.cellTop(ArsGrid.RESERVED_MIN_Z);
        int x1 = geo.cellLeft(ArsGrid.RESERVED_MAX_X + 1);
        int z1 = geo.cellTop(ArsGrid.RESERVED_MAX_Z + 1);
        graphics.fill(x0, z0, x1, z1, CONSOLE_FILL);
        graphics.fill(x0, z0, x1, z0 + 1, CONSOLE_EDGE);
        graphics.fill(x0, z1 - 1, x1, z1, CONSOLE_EDGE);
        graphics.fill(x0, z0, x0 + 1, z1, CONSOLE_EDGE);
        graphics.fill(x1 - 1, z0, x1, z1, CONSOLE_EDGE);
        // The console itself, at its anchor-local block position moved onto the grid.
        int cx = (int) Math.round(geo.anchorBlockX(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.getX() + 0.5));
        int cz = (int) Math.round(geo.anchorBlockZ(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.getZ() + 0.5));
        graphics.fill(cx - 2, cz - 2, cx + 3, cz + 3, CONSOLE_MARK);
        graphics.fill(cx - 1, cz - 1, cx + 2, cz + 2, CONSOLE_FILL);
        // The main doors: where players enter and leave the TARDIS.
        ArsMapState.Rect door = geo.entranceRect();
        graphics.fill(door.left() - 1, door.top() - 1, door.right() + 1, door.bottom() + 1, ENTRANCE_EDGE);
        graphics.fill(door.left(), door.top(), door.right(), door.bottom(), ENTRANCE_FILL);
        graphics.pose().pushMatrix();
        // Smaller than the room label so it fits between the doors and the console mark.
        graphics.pose().translate((door.left() + door.right()) / 2.0F, door.bottom() + 2);
        graphics.pose().scale(0.6F, 0.6F);
        graphics.centeredText(font, Component.translatable("dwm.gui.ars.entrance"), 0, 0, ENTRANCE_EDGE);
        graphics.pose().popMatrix();
        // Small text so the label stays inside the two-cell-wide room.
        Component label = Component.translatable("dwm.gui.ars.console_room");
        graphics.pose().pushMatrix();
        graphics.pose().translate((x0 + x1) / 2.0F, cz + 8);
        graphics.pose().scale(0.75F, 0.75F);
        graphics.centeredText(font, label, 0, 0, WHITE);
        graphics.pose().popMatrix();
    }

    /** Whether the cursor is on either console doorway. Both root links are canonically on a cell's west edge. */
    private boolean isNearRootDoorway(ArsMapState.MapGeometry geo, int mouseX, int mouseY) {
        for (ArsLayoutLogic.Link root : ArsLayoutLogic.ROOT_LINKS) {
            int x = geo.cellLeft(root.cell().x());
            int y = geo.cellTop(root.cell().z());
            if (mouseX >= x - 3 && mouseX <= x + 3 && mouseY >= y + APERTURE_FROM && mouseY < y + APERTURE_TO) {
                return true;
            }
        }
        return false;
    }

    private void drawGhost(GuiGraphicsExtractor graphics, ArsMapState.MapGeometry geo, ArsMapState.Ghost ghost) {
        ArsLayoutLogic.PlaceResult result = state.check(ghost, lookup, instabuild());
        boolean ok = result == ArsLayoutLogic.PlaceResult.OK;
        ArsPieceDefinition definition = lookup.apply(state.selectedPiece());
        int base = definition == null ? 0x808080 : definition.mapColor();
        Set<ArsCell> inGrid = new HashSet<>();
        for (ArsCell cell : ghost.cells()) {
            if (ArsGrid.inBounds(cell.x(), cell.z())) {
                inGrid.add(cell);
                fillCell(graphics, geo, cell, 0x90000000 | base);
                fillCell(graphics, geo, cell, ok ? GHOST_OK : GHOST_BAD);
            }
        }
        outline(graphics, geo, inGrid, ok ? GOOD : BAD, 1);
        for (ArsConnector connector : ghost.connectors()) {
            if (inGrid.contains(connector.cell())) {
                notch(graphics, geo, connector, WHITE);
            }
        }
    }

    private static void fillCell(GuiGraphicsExtractor graphics, ArsMapState.MapGeometry geo, ArsCell cell, int color) {
        int x = geo.cellLeft(cell.x());
        int y = geo.cellTop(cell.z());
        graphics.fill(x, y, x + CELL, y + CELL, color);
    }

    /** Draws the outer boundary of a set of cells, {@code thickness} pixels inside it. */
    private static void outline(GuiGraphicsExtractor graphics, ArsMapState.MapGeometry geo, Set<ArsCell> cells,
                                int color, int thickness) {
        for (ArsCell cell : cells) {
            int x = geo.cellLeft(cell.x());
            int y = geo.cellTop(cell.z());
            if (!cells.contains(cell.relative(Direction.NORTH))) {
                graphics.fill(x, y, x + CELL, y + thickness, color);
            }
            if (!cells.contains(cell.relative(Direction.SOUTH))) {
                graphics.fill(x, y + CELL - thickness, x + CELL, y + CELL, color);
            }
            if (!cells.contains(cell.relative(Direction.WEST))) {
                graphics.fill(x, y, x + thickness, y + CELL, color);
            }
            if (!cells.contains(cell.relative(Direction.EAST))) {
                graphics.fill(x + CELL - thickness, y, x + CELL, y + CELL, color);
            }
        }
    }

    /** A connector's doorway plug: a short bar on the inside of its cell edge. */
    private static void notch(GuiGraphicsExtractor graphics, ArsMapState.MapGeometry geo, ArsConnector connector, int color) {
        int x = geo.cellLeft(connector.cell().x());
        int y = geo.cellTop(connector.cell().z());
        int depth = 3;
        switch (connector.side()) {
            case NORTH -> graphics.fill(x + APERTURE_FROM, y, x + APERTURE_TO, y + depth, color);
            case SOUTH -> graphics.fill(x + APERTURE_FROM, y + CELL - depth, x + APERTURE_TO, y + CELL, color);
            case WEST -> graphics.fill(x, y + APERTURE_FROM, x + depth, y + APERTURE_TO, color);
            case EAST -> graphics.fill(x + CELL - depth, y + APERTURE_FROM, x + CELL, y + APERTURE_TO, color);
            default -> {
            }
        }
    }

    /** An open link: a bar straddling the shared edge, so the two pieces read as joined. */
    private static void doorway(GuiGraphicsExtractor graphics, ArsMapState.MapGeometry geo, ArsLayoutLogic.Link link, int color) {
        int x = geo.cellLeft(link.cell().x());
        int y = geo.cellTop(link.cell().z());
        if (link.side() == Direction.WEST) {
            graphics.fill(x - 3, y + APERTURE_FROM, x + 3, y + APERTURE_TO, color);
        } else {
            graphics.fill(x + APERTURE_FROM, y - 3, x + APERTURE_TO, y + 3, color);
        }
    }

    private static int darker(int argb) {
        int r = ((argb >> 16) & 0xFF) * 45 / 100;
        int g = ((argb >> 8) & 0xFF) * 45 / 100;
        int b = (argb & 0xFF) * 45 / 100;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // ---- palette and readouts ----------------------------------------------------------------------

    private void drawPalette(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x0 = panelLeft + SIDE_LEFT;
        int right = x0 + SIDE_WIDTH - 4;
        int sizeX = x0 + SIDE_WIDTH - 72;
        int headerY = panelTop + MAP_TOP + 2;
        graphics.text(font, Component.translatable("dwm.gui.ars.palette"), x0 + 4, headerY, LABEL, false);
        graphics.text(font, Component.translatable("dwm.gui.ars.size"), sizeX, headerY, LABEL, false);
        Component artronHeader = Component.translatable("dwm.gui.ars.artron_column");
        graphics.text(font, artronHeader, right - font.width(artronHeader), headerY, LABEL, false);

        Identifier hoveredRow = paletteRowAt(mouseX, mouseY);
        for (int i = 0; i < PALETTE_VISIBLE_ROWS && i + paletteScroll < palette.size(); i++) {
            Identifier id = palette.get(i + paletteScroll);
            ArsPieceDefinition definition = definitions.get(id);
            int y = panelTop + PALETTE_ROWS_TOP + i * PALETTE_ROW;
            if (id.equals(state.selectedPiece())) {
                graphics.fill(x0 + 1, y, x0 + SIDE_WIDTH - 1, y + PALETTE_ROW, ROW_SELECTED);
            } else if (id.equals(hoveredRow)) {
                graphics.fill(x0 + 1, y, x0 + SIDE_WIDTH - 1, y + PALETTE_ROW, ROW_HOVER);
            }
            int textY = y + 2;
            graphics.fill(x0 + 4, textY, x0 + 4 + SWATCH, textY + SWATCH, 0xFF000000 | definition.mapColor());
            graphics.text(font, Component.translatable(ArsService.pieceNameKey(id)), x0 + 15, textY, WHITE, false);
            graphics.text(font, Component.translatable("dwm.gui.ars.footprint", definition.width(), definition.depth()),
                    sizeX, textY, LABEL, false);
            String cost = Integer.toString(definition.artronCost());
            graphics.text(font, cost, right - font.width(cost), textY, LABEL, false);
        }
        if (palette.isEmpty()) {
            graphics.text(font, Component.translatable("dwm.gui.ars.palette_empty"), x0 + 4,
                    panelTop + PALETTE_ROWS_TOP + 2, LABEL, false);
        }
    }

    private void drawDetail(GuiGraphicsExtractor graphics, @Nullable ArsCell hovered) {
        int x = panelLeft + SIDE_LEFT + 5;
        int width = SIDE_WIDTH - 10;
        int y = panelTop + DETAIL_TOP + 4;
        boolean instabuild = instabuild();
        Component artron = instabuild
                ? Component.translatable("dwm.gui.ars.artron_free", state.artron(), ArtronLogic.CAPACITY)
                : Component.translatable("dwm.gui.ars.artron", state.artron(), ArtronLogic.CAPACITY);
        graphics.text(font, artron, x, y, WHITE, false);
        y += 12;

        Identifier selected = state.selectedPiece();
        ArsPieceDefinition selectedDefinition = selected == null ? null : lookup.apply(selected);
        if (selected != null && selectedDefinition != null) {
            graphics.text(font, Component.translatable("dwm.gui.ars.placing",
                    Component.translatable(ArsService.pieceNameKey(selected))), x, y, WHITE, false);
            y += 11;
            graphics.text(font, Component.translatable("dwm.gui.ars.cost_rotation",
                    selectedDefinition.artronCost(), state.rotation() * 90), x, y, LABEL, false);
            y += 11;
            Optional<ArsMapState.Ghost> ghost = state.ghostAt(hovered, lookup);
            if (ghost.isEmpty()) {
                wrapped(graphics, Component.translatable("dwm.gui.ars.placing_hint"), x, y, width, LABEL);
            } else {
                ArsLayoutLogic.PlaceResult result = state.check(ghost.get(), lookup, instabuild);
                if (result == ArsLayoutLogic.PlaceResult.OK) {
                    wrapped(graphics, Component.translatable("dwm.gui.ars.can_grow"), x, y, width, GOOD);
                } else {
                    wrapped(graphics, Component.translatable(result.translationKey()), x, y, width, BAD);
                }
            }
            return;
        }

        Optional<ArsPlacedPiece> placed = state.selectedPlaced(lookup);
        if (placed.isPresent()) {
            ArsPieceDefinition definition = ArsLayoutLogic.definitionOf(placed.get(), lookup);
            Identifier id = Identifier.parse(placed.get().pieceId);
            graphics.text(font, Component.translatable("dwm.gui.ars.selected",
                    Component.translatable(ArsService.pieceNameKey(id))), x, y, WHITE, false);
            y += 11;
            graphics.text(font, Component.translatable("dwm.gui.ars.refund", ArsLayoutLogic.refundFor(definition)),
                    x, y, LABEL, false);
            y += 11;
            ArsLayoutLogic.RemoveResult removal = state.removeCheck(lookup);
            if (removal == ArsLayoutLogic.RemoveResult.OK) {
                wrapped(graphics, Component.translatable("dwm.gui.ars.can_jettison"), x, y, width, GOOD);
            } else {
                wrapped(graphics, Component.translatable(removal.translationKey()), x, y, width, BAD);
            }
            return;
        }
        wrapped(graphics, Component.translatable("dwm.gui.ars.nothing_selected"), x, y, width, LABEL);
    }

    private void wrapped(GuiGraphicsExtractor graphics, Component text, int x, int y, int width, int color) {
        for (FormattedCharSequence line : font.split(text, width)) {
            graphics.text(font, line, x, y, color, false);
            y += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
