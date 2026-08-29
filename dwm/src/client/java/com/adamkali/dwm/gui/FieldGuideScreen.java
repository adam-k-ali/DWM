package com.adamkali.dwm.gui;

import com.adamkali.dwm.gui.layout.Columns;
import com.adamkali.dwm.gui.layout.FillWidget;
import com.adamkali.dwm.gui.layout.Layouts;
import com.adamkali.dwm.gui.layout.Stack;
import com.adamkali.dwm.guide.FieldGuideBodyPaginator;
import com.adamkali.dwm.guide.FieldGuideBodyWidget;
import com.adamkali.dwm.guide.FieldGuideBookLayout;
import com.adamkali.dwm.guide.FieldGuideCatalog;
import com.adamkali.dwm.guide.FieldGuideChapter;
import com.adamkali.dwm.guide.FieldGuidePage;
import com.adamkali.dwm.guide.FieldGuideRecipePanel;
import com.adamkali.dwm.guide.FieldGuideRecipeWidget;
import com.adamkali.dwm.guide.FieldGuideVariantGroups;
import com.adamkali.dwm.guide.FieldGuideVariantWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FieldGuideScreen extends Screen {
    private final List<AbstractWidget> contentWidgets = new ArrayList<>();
    private @Nullable PageButton backPageButton;
    private @Nullable PageButton forwardPageButton;

    private @Nullable FieldGuideChapter selectedChapter;
    private @Nullable FieldGuidePage selectedPage;
    private FieldGuideRecipePanel.Station selectedStation = FieldGuideRecipePanel.Station.CRAFTING;
    private int selectedGroupIndex;
    private boolean selectedZeiton;
    private int visualPageIndex;
    private int bookLeft;
    private int bookTop;

    public FieldGuideScreen() {
        super(Component.translatable("dwm.guide.title"));
    }

    public FieldGuideScreen(@Nullable FieldGuidePage initialPage) {
        super(Component.translatable("dwm.guide.title"));
        selectedPage = initialPage;
    }

    @Override
    protected void init() {
        bookLeft = FieldGuideBookLayout.bookLeft(width);
        bookTop = FieldGuideBookLayout.bookTop(height);
        contentWidgets.clear();

        List<FieldGuideChapter> chapters = chapters();
        if (selectedPage == null) {
            if (!chapters.isEmpty() && !chapters.getFirst().pages().isEmpty()) {
                selectedChapter = chapters.getFirst();
                selectedPage = selectedChapter.pages().getFirst();
            }
        } else if (selectedChapter == null) {
            for (FieldGuideChapter chapter : chapters) {
                if (chapter.pages().contains(selectedPage)) {
                    selectedChapter = chapter;
                    break;
                }
            }
            if (selectedChapter == null) {
                selectedPage = null;
            }
        }

        backPageButton = addRenderableWidget(new PageButton(
                bookLeft + FieldGuideBookLayout.PAGE_BACK_X,
                bookTop + FieldGuideBookLayout.PAGE_BUTTON_Y,
                false,
                button -> turnPage(-1),
                true
        ));
        backPageButton.setMessage(Component.translatable("dwm.guide.page.previous"));
        forwardPageButton = addRenderableWidget(new PageButton(
                bookLeft + FieldGuideBookLayout.PAGE_FORWARD_X,
                bookTop + FieldGuideBookLayout.PAGE_BUTTON_Y,
                true,
                button -> turnPage(1),
                true
        ));
        forwardPageButton.setMessage(Component.translatable("dwm.guide.page.next"));

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(
                        width / 2 - FieldGuideBookLayout.DONE_BUTTON_WIDTH / 2,
                        FieldGuideBookLayout.doneButtonTop(bookTop),
                        FieldGuideBookLayout.DONE_BUTTON_WIDTH,
                        20
                )
                .build());

        rebuildContent();
    }

    private void rebuildContent() {
        contentWidgets.forEach(this::removeWidget);
        contentWidgets.clear();
        if (selectedChapter == null || selectedPage == null) {
            return;
        }

        List<FieldGuideBodyPaginator.Slice> slices = visualSlices(selectedPage);
        visualPageIndex = Math.min(Math.max(visualPageIndex, 0), slices.size() - 1);
        List<FieldGuideVariantGroups.Group> groups = craftingGroups(selectedPage);
        if (!groups.isEmpty()) {
            selectedGroupIndex = Math.min(Math.max(selectedGroupIndex, 0), groups.size() - 1);
        }

        contentWidgets.addAll(Layouts.mount(
                buildLeftPage(),
                bookLeft + FieldGuideBookLayout.LEFT_PAGE_X,
                bookTop + FieldGuideBookLayout.INDEX_HEADER_Y,
                this::addRenderableWidget
        ));
        contentWidgets.addAll(Layouts.mount(
                buildRightPage(slices.get(visualPageIndex)),
                bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X,
                bookTop + FieldGuideBookLayout.RIGHT_PAGE_TOP,
                this::addRenderableWidget
        ));

        StringWidget indicator = coloured(pageIndicator(), FieldGuideBookLayout.MUTED_TEXT_COLOR);
        indicator.setPosition(
                bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X + 28,
                bookTop + FieldGuideBookLayout.RIGHT_INDICATOR_Y
        );
        addRenderableWidget(indicator);
        contentWidgets.add(indicator);
        updateNavigationState();
    }

    private Stack buildLeftPage() {
        Stack chapters = Stack.vertical(FieldGuideBookLayout.CHAPTER_BUTTON_GAP);
        for (FieldGuideChapter chapter : chapters()) {
            FieldGuideChapter targetChapter = chapter;
            boolean selected = chapter.equals(selectedChapter);
            chapters.add(new PlainTextButton(
                    0,
                    0,
                    FieldGuideBookLayout.LEFT_PAGE_WIDTH,
                    FieldGuideBookLayout.CHAPTER_ENTRY_HEIGHT,
                    styledChapterLabel(chapter.titleKey(), selected),
                    button -> selectChapter(targetChapter),
                    font
            ));
        }

        Stack pages = Stack.vertical(0);
        if (selectedChapter != null) {
            for (FieldGuidePage page : selectedChapter.pages()) {
                FieldGuidePage targetPage = page;
                boolean selected = page.equals(selectedPage);
                pages.add(new PlainTextButton(
                        0,
                        0,
                        FieldGuideBookLayout.LEFT_PAGE_WIDTH - FieldGuideBookLayout.PAGE_ENTRY_INDENT,
                        FieldGuideBookLayout.PAGE_ENTRY_HEIGHT,
                        styledPageLabel(page.titleKey(), selected),
                        button -> selectPage(targetPage),
                        font
                ), settings -> settings.paddingLeft(FieldGuideBookLayout.PAGE_ENTRY_INDENT));
            }
        }

        Stack pagesSection = Stack.vertical(FieldGuideBookLayout.STACK_GAP);
        pagesSection.add(coloured(Component.literal("PAGES"), FieldGuideBookLayout.INDEX_HEADER_COLOR));
        pagesSection.add(new FillWidget(
                FieldGuideBookLayout.LEFT_PAGE_WIDTH - 6,
                FieldGuideBookLayout.HAIRLINE_HEIGHT,
                FieldGuideBookLayout.LEFT_HAIRLINE_COLOR
        ));
        pagesSection.add(pages);

        Stack left = Stack.vertical(FieldGuideBookLayout.SECTION_GAP);
        left.add(coloured(Component.translatable("dwm.guide.index.header"), FieldGuideBookLayout.INDEX_HEADER_COLOR));
        left.add(chapters);
        left.add(pagesSection);
        return left;
    }

    private Stack buildRightPage(FieldGuideBodyPaginator.Slice slice) {
        FieldGuidePage page = selectedPage;
        List<FormattedCharSequence> lines = wrappedBody(page);
        int end = slice.start() + slice.count();
        List<FormattedCharSequence> visible = lines.subList(slice.start(), end);

        Stack content = Stack.vertical(FieldGuideBookLayout.STACK_GAP);
        content.add(coloured(
                Component.translatable(selectedChapter.titleKey()),
                FieldGuideBookLayout.MUTED_TEXT_COLOR
        ));
        content.add(new StringWidget(
                Component.translatable(page.titleKey()).copy().withStyle(
                        Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.TITLE_COLOR)
                ),
                font
        ));
        content.add(new FillWidget(
                FieldGuideBookLayout.RIGHT_PAGE_WIDTH,
                FieldGuideBookLayout.HAIRLINE_HEIGHT,
                FieldGuideBookLayout.ACCENT_COLOR
        ));
        content.add(new FieldGuideBodyWidget(font, visible));

        if (slice.recipe()) {
            List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
            if (!stations.isEmpty()) {
                content.add(buildRecipeHeader(page, stations));
                content.add(
                        new FieldGuideRecipeWidget(minecraft, page, selectedStation, currentCraftingVariantIndex(page)),
                        settings -> settings.alignHorizontallyCenter()
                );
            }
            if (page.patternPage()) {
                content.add(new MultiLineTextWidget(
                        Component.translatable("dwm.guide.pattern.all_colours").copy().withStyle(
                                Style.EMPTY.withColor(FieldGuideBookLayout.TEXT_COLOR).withoutShadow()
                        ),
                        font
                ).setMaxWidth(FieldGuideBookLayout.RIGHT_PAGE_WIDTH).setMaxRows(FieldGuideBookLayout.PATTERN_MAX_LINES));
            }
        }
        return content;
    }

    private Stack buildRecipeHeader(FieldGuidePage page, List<FieldGuideRecipePanel.Station> stations) {
        Stack header = Stack.vertical(FieldGuideBookLayout.STACK_GAP);
        if (stations.size() > 1) {
            Stack tabs = Columns.of(FieldGuideBookLayout.STATION_TAB_GAP);
            for (FieldGuideRecipePanel.Station station : stations) {
                FieldGuideRecipePanel.Station target = station;
                boolean selected = station == selectedStation;
                tabs.add(new PlainTextButton(
                        0,
                        0,
                        FieldGuideBookLayout.STATION_TAB_WIDTH,
                        FieldGuideBookLayout.STATION_TAB_HEIGHT,
                        station.label().copy().withStyle(selected
                                ? Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.PAGE_SELECTED_COLOR)
                                : Style.EMPTY.withColor(FieldGuideBookLayout.PAGE_UNSELECTED_COLOR)),
                        button -> {
                            selectedStation = target;
                            rebuildContent();
                        },
                        font
                ));
            }
            header.add(tabs);
        } else {
            header.add(new StringWidget(
                    selectedStation.label().copy().append(" RECIPE").withStyle(
                            Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.INDEX_HEADER_COLOR)
                    ),
                    font
            ));
        }

        if (selectedStation == FieldGuideRecipePanel.Station.CRAFTING) {
            List<FieldGuideVariantGroups.Group> groups = craftingGroups(page);
            if (groups.size() > 1) {
                header.add(wrappedVariantIcons(groups));
            }
            if (FieldGuideVariantGroups.hasPathToggle(groups)) {
                header.add(pathToggleRow());
            }
        }
        return header;
    }

    private Stack wrappedVariantIcons(List<FieldGuideVariantGroups.Group> groups) {
        int columns = FieldGuideBookLayout.variantColumns();
        Stack rows = Stack.vertical(FieldGuideBookLayout.VARIANT_ICON_GAP);
        Stack row = Columns.of(FieldGuideBookLayout.VARIANT_ICON_GAP);
        int inRow = 0;
        for (int index = 0; index < groups.size(); index++) {
            if (inRow == columns) {
                rows.add(row);
                row = Columns.of(FieldGuideBookLayout.VARIANT_ICON_GAP);
                inRow = 0;
            }
            int groupIndex = index;
            FieldGuideVariantGroups.Group group = groups.get(index);
            row.add(new FieldGuideVariantWidget(
                    minecraft,
                    FieldGuideVariantGroups.recipeFor(group, selectedZeiton),
                    index == selectedGroupIndex,
                    () -> {
                        selectedGroupIndex = groupIndex;
                        rebuildContent();
                    }
            ));
            inRow++;
        }
        if (inRow > 0) {
            rows.add(row);
        }
        return rows;
    }

    private Stack pathToggleRow() {
        Stack paths = Columns.of(FieldGuideBookLayout.STATION_TAB_GAP);
        paths.add(pathButton("dwm.guide.recipe.path.vanilla", false));
        paths.add(pathButton("dwm.guide.recipe.path.zeiton", true));
        return paths;
    }

    private PlainTextButton pathButton(String labelKey, boolean zeiton) {
        boolean selected = selectedZeiton == zeiton;
        return new PlainTextButton(
                0,
                0,
                FieldGuideBookLayout.STATION_TAB_WIDTH,
                FieldGuideBookLayout.STATION_TAB_HEIGHT,
                Component.translatable(labelKey).copy().withStyle(selected
                        ? Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.PAGE_SELECTED_COLOR)
                        : Style.EMPTY.withColor(FieldGuideBookLayout.PAGE_UNSELECTED_COLOR)),
                button -> {
                    selectedZeiton = zeiton;
                    rebuildContent();
                },
                font
        );
    }

    private List<FieldGuideVariantGroups.Group> craftingGroups(FieldGuidePage page) {
        return FieldGuideVariantGroups.group(
                page.craftingRecipes(),
                recipe -> minecraft == null ? recipe : FieldGuideRecipePanel.resultId(minecraft, recipe)
        );
    }

    private int currentCraftingVariantIndex(FieldGuidePage page) {
        List<FieldGuideVariantGroups.Group> groups = craftingGroups(page);
        if (groups.isEmpty()) {
            return 0;
        }
        int groupIndex = Math.clamp(selectedGroupIndex, 0, groups.size() - 1);
        Identifier recipe = FieldGuideVariantGroups.recipeFor(groups.get(groupIndex), selectedZeiton);
        int index = page.craftingRecipes().indexOf(recipe);
        return Math.max(0, index);
    }

    private List<FormattedCharSequence> wrappedBody(FieldGuidePage page) {
        return font.split(
                Component.translatable(page.bodyKey()).copy().withStyle(
                        Style.EMPTY.withColor(FieldGuideBookLayout.TEXT_COLOR).withoutShadow()
                ),
                FieldGuideBookLayout.RIGHT_PAGE_WIDTH
        );
    }

    private int firstPageRows(FieldGuidePage page) {
        boolean recipe = !FieldGuideRecipePanel.availableStations(page).isEmpty();
        boolean crafting = recipe
                && (page != selectedPage || selectedStation == FieldGuideRecipePanel.Station.CRAFTING);
        int variantIcons = 0;
        boolean pathToggle = false;
        if (crafting) {
            List<FieldGuideVariantGroups.Group> groups = craftingGroups(page);
            if (groups.size() > 1) {
                variantIcons = groups.size();
            }
            pathToggle = FieldGuideVariantGroups.hasPathToggle(groups);
        }
        boolean pattern = recipe && page.patternPage();
        return FieldGuideBookLayout.bodyMaxRows(recipe, variantIcons, pathToggle, pattern);
    }

    private static int continuationRows() {
        return FieldGuideBookLayout.bodyMaxRows(false, 0, false, false);
    }

    private List<FieldGuideBodyPaginator.Slice> visualSlices(FieldGuidePage page) {
        return FieldGuideBodyPaginator.paginate(
                wrappedBody(page).size(),
                firstPageRows(page),
                continuationRows()
        );
    }

    private int visualPageCount(FieldGuidePage page) {
        return visualSlices(page).size();
    }

    private StringWidget coloured(Component text, int color) {
        return new StringWidget(text.copy().withStyle(Style.EMPTY.withColor(color)), font);
    }

    private Component pageIndicator() {
        int visualOffset = 0;
        int total = 0;
        for (FieldGuidePage page : selectedChapter.pages()) {
            int count = visualPageCount(page);
            if (page.equals(selectedPage)) {
                visualOffset = total + visualPageIndex;
            }
            total += count;
        }
        return Component.translatable(
                "dwm.guide.page.indicator",
                Component.translatable(selectedChapter.titleKey()),
                visualOffset + 1,
                total
        );
    }

    private List<FieldGuideChapter> chapters() {
        if (minecraft == null || minecraft.level == null) {
            return List.of();
        }
        return FieldGuideCatalog.chapters(minecraft.level.registryAccess());
    }

    private static Component styledChapterLabel(String titleKey, boolean selected) {
        return Component.translatable(titleKey).withStyle(selected
                ? Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.CHAPTER_SELECTED_COLOR)
                : Style.EMPTY.withColor(FieldGuideBookLayout.CHAPTER_UNSELECTED_COLOR));
    }

    private static Component styledPageLabel(String titleKey, boolean selected) {
        return Component.translatable(titleKey).withStyle(selected
                ? Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.PAGE_SELECTED_COLOR)
                : Style.EMPTY.withColor(FieldGuideBookLayout.PAGE_UNSELECTED_COLOR));
    }

    private void selectChapter(FieldGuideChapter chapter) {
        selectedChapter = chapter;
        selectPage(chapter.pages().getFirst());
    }

    private void selectPage(FieldGuidePage page) {
        selectPage(page, 0);
    }

    private void selectPage(FieldGuidePage page, int visualIndex) {
        selectedPage = page;
        selectedChapter = FieldGuideCatalog.chapterForPage(chapters(), page);
        selectedGroupIndex = 0;
        selectedZeiton = false;
        visualPageIndex = Math.max(0, visualIndex);
        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
        if (!stations.isEmpty() && !stations.contains(selectedStation)) {
            selectedStation = stations.getFirst();
        }
        rebuildContent();
    }

    private void turnPage(int delta) {
        if (selectedChapter == null || selectedPage == null) {
            return;
        }
        List<FieldGuideBodyPaginator.Slice> slices = visualSlices(selectedPage);
        int nextVisual = visualPageIndex + delta;
        if (nextVisual >= 0 && nextVisual < slices.size()) {
            visualPageIndex = nextVisual;
            rebuildContent();
            return;
        }
        int index = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage) + delta;
        if (index < 0 || index >= selectedChapter.pages().size()) {
            return;
        }
        FieldGuidePage neighbour = selectedChapter.pages().get(index);
        int neighbourVisual = delta > 0 ? 0 : visualPageCount(neighbour) - 1;
        selectPage(neighbour, neighbourVisual);
    }

    private void updateNavigationState() {
        if (selectedChapter == null || selectedPage == null || backPageButton == null || forwardPageButton == null) {
            return;
        }
        int index = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage);
        int sliceCount = visualSlices(selectedPage).size();
        backPageButton.active = visualPageIndex > 0 || index > 0;
        forwardPageButton.active = visualPageIndex < sliceCount - 1 || index < selectedChapter.pages().size() - 1;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        renderCatalogBackground(graphics);
    }

    private void renderCatalogBackground(GuiGraphicsExtractor graphics) {
        int right = bookLeft + FieldGuideBookLayout.BOOK_WIDTH;
        int bottom = bookTop + FieldGuideBookLayout.BOOK_HEIGHT;

        graphics.fill(bookLeft + 4, bookTop + 5, right + 5, bottom + 6, 0x70000000);
        graphics.fill(bookLeft, bookTop, right, bottom, FieldGuideBookLayout.COVER_EDGE_COLOR);
        graphics.fill(bookLeft + 3, bookTop + 3, right - 3, bottom - 3, FieldGuideBookLayout.COVER_COLOR);

        int leftPageRight = bookLeft + FieldGuideBookLayout.GUTTER_X;
        int rightPageLeft = bookLeft + FieldGuideBookLayout.GUTTER_X + FieldGuideBookLayout.GUTTER_WIDTH;
        graphics.fill(bookLeft + 8, bookTop + 7, leftPageRight, bottom - 7, FieldGuideBookLayout.PAGE_COLOR);
        graphics.fill(rightPageLeft, bookTop + 7, right - 8, bottom - 7, FieldGuideBookLayout.PAGE_COLOR);
        graphics.fill(bookLeft + 11, bookTop + 10, leftPageRight - 3, bottom - 10, FieldGuideBookLayout.PAGE_INSET_COLOR);
        graphics.fill(rightPageLeft + 3, bookTop + 10, right - 11, bottom - 10, FieldGuideBookLayout.PAGE_INSET_COLOR);

        graphics.fill(leftPageRight, bookTop + 5, rightPageLeft, bottom - 5, FieldGuideBookLayout.GUTTER_COLOR);
        graphics.fill(leftPageRight + 3, bookTop + 8, rightPageLeft - 3, bottom - 8, 0x55301F18);

        graphics.fill(
                bookLeft + 12,
                bookTop + FieldGuideBookLayout.HEADER_Y,
                leftPageRight - 6,
                bookTop + FieldGuideBookLayout.HEADER_Y + FieldGuideBookLayout.HEADER_HEIGHT,
                FieldGuideBookLayout.HEADER_COLOR
        );
        graphics.fill(
                rightPageLeft + 6,
                bookTop + FieldGuideBookLayout.HEADER_Y,
                right - 12,
                bookTop + FieldGuideBookLayout.HEADER_Y + 3,
                FieldGuideBookLayout.ACCENT_COLOR
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(
                font,
                Component.translatable("dwm.guide.title"),
                bookLeft + FieldGuideBookLayout.LEFT_PAGE_X + 5,
                bookTop + FieldGuideBookLayout.HEADER_Y + 7,
                0xFFFFFFFF,
                false
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}
