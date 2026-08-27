package com.adamkali.dwm.gui;

import com.adamkali.dwm.gui.layout.Columns;
import com.adamkali.dwm.gui.layout.FillWidget;
import com.adamkali.dwm.gui.layout.Layouts;
import com.adamkali.dwm.gui.layout.Stack;
import com.adamkali.dwm.guide.FieldGuideBookLayout;
import com.adamkali.dwm.guide.FieldGuideCatalog;
import com.adamkali.dwm.guide.FieldGuideChapter;
import com.adamkali.dwm.guide.FieldGuidePage;
import com.adamkali.dwm.guide.FieldGuideRecipePanel;
import com.adamkali.dwm.guide.FieldGuideRecipeWidget;
import com.adamkali.dwm.guide.FieldGuideVariantWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
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
    private int selectedCraftingVariantIndex;
    private int bookLeft;
    private int bookTop;

    public FieldGuideScreen() {
        super(Component.translatable("dwm.guide.title"));
    }

    public FieldGuideScreen(@Nullable FieldGuidePage initialPage) {
        super(Component.translatable("dwm.guide.title"));
        if (initialPage != null) {
            selectedChapter = FieldGuideCatalog.chapterForPage(initialPage);
            selectedPage = initialPage;
        }
    }

    @Override
    protected void init() {
        bookLeft = FieldGuideBookLayout.bookLeft(width);
        bookTop = FieldGuideBookLayout.bookTop(height);
        contentWidgets.clear();

        if (selectedPage == null) {
            selectedChapter = FieldGuideCatalog.chapters().getFirst();
            selectedPage = selectedChapter.pages().getFirst();
        } else if (selectedChapter == null) {
            selectedChapter = FieldGuideCatalog.chapterForPage(selectedPage);
        }

        backPageButton = addRenderableWidget(new PageButton(
                bookLeft + FieldGuideBookLayout.PAGE_BACK_X,
                bookTop + FieldGuideBookLayout.PAGE_BUTTON_Y,
                false,
                button -> turnPage(-1),
                true
        ));
        forwardPageButton = addRenderableWidget(new PageButton(
                bookLeft + FieldGuideBookLayout.PAGE_FORWARD_X,
                bookTop + FieldGuideBookLayout.PAGE_BUTTON_Y,
                true,
                button -> turnPage(1),
                true
        ));

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(
                        width / 2 - FieldGuideBookLayout.DONE_BUTTON_WIDTH / 2,
                        FieldGuideBookLayout.doneButtonTop(bookTop),
                        FieldGuideBookLayout.DONE_BUTTON_WIDTH,
                        20
                )
                .build());

        updateNavigationState();
        rebuildContent();
    }

    private void rebuildContent() {
        contentWidgets.forEach(this::removeWidget);
        contentWidgets.clear();
        if (selectedChapter == null || selectedPage == null) {
            return;
        }

        contentWidgets.addAll(Layouts.mount(
                buildLeftPage(),
                bookLeft + FieldGuideBookLayout.LEFT_PAGE_X,
                bookTop + FieldGuideBookLayout.INDEX_HEADER_Y,
                this::addRenderableWidget
        ));
        contentWidgets.addAll(Layouts.mount(
                buildRightPage(),
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
    }

    private Stack buildLeftPage() {
        Stack chapters = Stack.vertical(FieldGuideBookLayout.CHAPTER_BUTTON_GAP);
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
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

    private Stack buildRightPage() {
        FieldGuidePage page = selectedPage;
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
        content.add(new MultiLineTextWidget(
                Component.translatable(page.bodyKey()).copy().withStyle(
                        Style.EMPTY.withColor(FieldGuideBookLayout.TEXT_COLOR)
                ),
                font
        ).setMaxWidth(FieldGuideBookLayout.RIGHT_PAGE_WIDTH).setMaxRows(bodyMaxRows(page)));

        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
        if (!stations.isEmpty()) {
            content.add(buildRecipeHeader(page, stations));
            content.add(
                    new FieldGuideRecipeWidget(minecraft, page, selectedStation, selectedCraftingVariantIndex),
                    settings -> settings.alignHorizontallyCenter()
            );
        }
        if (page.patternPage()) {
            content.add(new MultiLineTextWidget(
                    Component.translatable("dwm.guide.pattern.all_colours").copy().withStyle(
                            Style.EMPTY.withColor(FieldGuideBookLayout.TEXT_COLOR)
                    ),
                    font
            ).setMaxWidth(FieldGuideBookLayout.RIGHT_PAGE_WIDTH).setMaxRows(FieldGuideBookLayout.PATTERN_MAX_LINES));
        }
        return content;
    }

    private FrameLayout buildRecipeHeader(FieldGuidePage page, List<FieldGuideRecipePanel.Station> stations) {
        boolean showVariants = selectedStation == FieldGuideRecipePanel.Station.CRAFTING
                && page.craftingRecipes().size() > 1;
        int headerHeight = showVariants
                ? FieldGuideBookLayout.VARIANT_SLOT_SIZE
                : FieldGuideBookLayout.LINE_HEIGHT;
        FrameLayout header = new FrameLayout(FieldGuideBookLayout.RIGHT_PAGE_WIDTH, headerHeight);

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
            header.addChild(tabs, settings -> settings.alignHorizontallyLeft().alignVerticallyMiddle());
        } else {
            header.addChild(
                    new StringWidget(
                            selectedStation.label().copy().append(" RECIPE").withStyle(
                                    Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.INDEX_HEADER_COLOR)
                            ),
                            font
                    ),
                    settings -> settings.alignHorizontallyLeft().alignVerticallyMiddle()
            );
        }

        if (showVariants) {
            Stack variants = Columns.of(FieldGuideBookLayout.VARIANT_ICON_GAP);
            List<Identifier> recipes = page.craftingRecipes();
            for (int index = 0; index < recipes.size(); index++) {
                int variantIndex = index;
                variants.add(new FieldGuideVariantWidget(
                        minecraft,
                        recipes.get(index),
                        index == selectedCraftingVariantIndex,
                        () -> {
                            selectedCraftingVariantIndex = variantIndex;
                            rebuildContent();
                        }
                ));
            }
            header.addChild(variants, settings -> settings.alignHorizontallyRight().alignVerticallyMiddle());
        }
        return header;
    }

    private int bodyMaxRows(FieldGuidePage page) {
        int children = 4;
        int intrinsic = FieldGuideBookLayout.LINE_HEIGHT * 2 + FieldGuideBookLayout.HAIRLINE_HEIGHT;
        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
        if (!stations.isEmpty()) {
            children += 2;
            boolean variants = selectedStation == FieldGuideRecipePanel.Station.CRAFTING
                    && page.craftingRecipes().size() > 1;
            intrinsic += variants
                    ? FieldGuideBookLayout.VARIANT_SLOT_SIZE
                    : FieldGuideBookLayout.LINE_HEIGHT;
            intrinsic += FieldGuideRecipePanel.PANEL_HEIGHT;
        }
        if (page.patternPage()) {
            children += 1;
            intrinsic += FieldGuideBookLayout.PATTERN_MAX_LINES * FieldGuideBookLayout.LINE_HEIGHT;
        }
        int reserved = intrinsic + (children - 1) * FieldGuideBookLayout.STACK_GAP;
        int budget = FieldGuideBookLayout.rightPageContentHeight();
        return Math.max(1, (budget - reserved) / FieldGuideBookLayout.LINE_HEIGHT);
    }

    private StringWidget coloured(Component text, int color) {
        return new StringWidget(text.copy().withStyle(Style.EMPTY.withColor(color)), font);
    }

    private Component pageIndicator() {
        int pageIndex = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage) + 1;
        return Component.translatable(
                "dwm.guide.page.indicator",
                Component.translatable(selectedChapter.titleKey()),
                pageIndex,
                selectedChapter.pages().size()
        );
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
        selectedPage = page;
        selectedChapter = FieldGuideCatalog.chapterForPage(page);
        selectedCraftingVariantIndex = 0;
        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
        if (!stations.isEmpty() && !stations.contains(selectedStation)) {
            selectedStation = stations.getFirst();
        }
        rebuildContent();
        updateNavigationState();
    }

    private void turnPage(int delta) {
        if (selectedChapter == null || selectedPage == null) {
            return;
        }
        int index = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage) + delta;
        if (index < 0 || index >= selectedChapter.pages().size()) {
            return;
        }
        selectPage(selectedChapter.pages().get(index));
    }

    private void updateNavigationState() {
        if (selectedChapter == null || selectedPage == null || backPageButton == null || forwardPageButton == null) {
            return;
        }
        int index = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage);
        backPageButton.active = index > 0;
        forwardPageButton.active = index < selectedChapter.pages().size() - 1;
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
