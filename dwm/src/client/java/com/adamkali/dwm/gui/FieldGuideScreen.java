package com.adamkali.dwm.gui;

import com.adamkali.dwm.guide.FieldGuideBookLayout;
import com.adamkali.dwm.guide.FieldGuideCatalog;
import com.adamkali.dwm.guide.FieldGuideChapter;
import com.adamkali.dwm.guide.FieldGuidePage;
import com.adamkali.dwm.guide.FieldGuideRecipePanel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FieldGuideScreen extends Screen {
    private static final int BODY_TEXT_MAX_LINES_WITH_RECIPE = 2;
    private static final int BODY_TEXT_MAX_LINES_WITHOUT_RECIPE = 6;

    private final List<PlainTextButton> chapterButtons = new ArrayList<>();
    private final List<PlainTextButton> pageButtons = new ArrayList<>();
    private final List<PlainTextButton> stationTabButtons = new ArrayList<>();
    private @Nullable PageButton backPageButton;
    private @Nullable PageButton forwardPageButton;

    private @Nullable FieldGuideChapter selectedChapter;
    private @Nullable FieldGuidePage selectedPage;
    private FieldGuideRecipePanel.Station selectedStation = FieldGuideRecipePanel.Station.CRAFTING;
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

        clearIndexWidgets();
        rebuildIndexWidgets();

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
        rebuildStationTabs();
    }

    private void clearIndexWidgets() {
        chapterButtons.forEach(this::removeWidget);
        chapterButtons.clear();
        pageButtons.forEach(this::removeWidget);
        pageButtons.clear();
        stationTabButtons.forEach(this::removeWidget);
        stationTabButtons.clear();
    }

    private void rebuildIndexWidgets() {
        int chapterY = bookTop + FieldGuideBookLayout.INDEX_CONTENT_Y;
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
            FieldGuideChapter targetChapter = chapter;
            boolean selected = chapter.equals(selectedChapter);
            PlainTextButton chapterButton = new PlainTextButton(
                    bookLeft + FieldGuideBookLayout.LEFT_PAGE_X,
                    chapterY,
                    FieldGuideBookLayout.LEFT_PAGE_WIDTH,
                    FieldGuideBookLayout.CHAPTER_ENTRY_HEIGHT,
                    styledChapterLabel(chapter.titleKey(), selected),
                    button -> selectChapter(targetChapter),
                    font
            );
            addRenderableWidget(chapterButton);
            chapterButtons.add(chapterButton);
            chapterY += FieldGuideBookLayout.CHAPTER_ENTRY_HEIGHT;
        }

        if (selectedChapter != null) {
            int pageY = chapterY + 2;
            for (FieldGuidePage page : selectedChapter.pages()) {
                FieldGuidePage targetPage = page;
                boolean selected = page.equals(selectedPage);
                PlainTextButton pageButton = new PlainTextButton(
                        bookLeft + FieldGuideBookLayout.LEFT_PAGE_X + FieldGuideBookLayout.PAGE_ENTRY_INDENT,
                        pageY,
                        FieldGuideBookLayout.LEFT_PAGE_WIDTH - FieldGuideBookLayout.PAGE_ENTRY_INDENT,
                        FieldGuideBookLayout.PAGE_ENTRY_HEIGHT,
                        styledPageLabel(page.titleKey(), selected),
                        button -> selectPage(targetPage),
                        font
                );
                addRenderableWidget(pageButton);
                pageButtons.add(pageButton);
                pageY += FieldGuideBookLayout.PAGE_ENTRY_HEIGHT;
            }
        }
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
        init();
    }

    private void selectPage(FieldGuidePage page) {
        selectedPage = page;
        selectedChapter = FieldGuideCatalog.chapterForPage(page);
        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
        if (!stations.isEmpty() && !stations.contains(selectedStation)) {
            selectedStation = stations.getFirst();
        }
        rebuildIndexWidgets();
        updateNavigationState();
        rebuildStationTabs();
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

    private void rebuildStationTabs() {
        stationTabButtons.forEach(this::removeWidget);
        stationTabButtons.clear();
        if (selectedPage == null) {
            return;
        }
        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(selectedPage);
        if (stations.size() <= 1) {
            return;
        }
        int tabX = bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X;
        int tabY = bookTop + FieldGuideBookLayout.RIGHT_RECIPE_Y - 11;
        for (FieldGuideRecipePanel.Station station : stations) {
            FieldGuideRecipePanel.Station target = station;
            boolean selected = station == selectedStation;
            PlainTextButton tab = new PlainTextButton(
                    tabX,
                    tabY,
                    42,
                    9,
                    station.label().copy().withStyle(selected
                            ? Style.EMPTY.withBold(true).withColor(FieldGuideBookLayout.PAGE_SELECTED_COLOR)
                            : Style.EMPTY.withColor(FieldGuideBookLayout.PAGE_UNSELECTED_COLOR)),
                    button -> {
                        selectedStation = target;
                        rebuildStationTabs();
                    },
                    font
            );
            addRenderableWidget(tab);
            stationTabButtons.add(tab);
            tabX += 44;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BookViewScreen.BOOK_LOCATION,
                bookLeft,
                bookTop,
                0.0F,
                0.0F,
                FieldGuideBookLayout.BOOK_WIDTH,
                FieldGuideBookLayout.BOOK_HEIGHT,
                256,
                256
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (selectedChapter == null || selectedPage == null) {
            return;
        }

        int leftX = bookLeft + FieldGuideBookLayout.LEFT_PAGE_X;
        int rightX = bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X;

        graphics.text(
                font,
                Component.translatable("dwm.guide.index.header"),
                leftX,
                bookTop + FieldGuideBookLayout.INDEX_HEADER_Y,
                FieldGuideBookLayout.INDEX_HEADER_COLOR,
                false
        );

        graphics.text(
                font,
                Component.translatable(selectedPage.titleKey()),
                rightX,
                bookTop + FieldGuideBookLayout.RIGHT_TITLE_Y,
                FieldGuideBookLayout.TITLE_COLOR,
                false
        );
        renderWrappedBody(
                graphics,
                Component.translatable(selectedPage.bodyKey()),
                rightX,
                bookTop + FieldGuideBookLayout.RIGHT_BODY_Y,
                FieldGuideBookLayout.RIGHT_PAGE_WIDTH,
                FieldGuideRecipePanel.availableStations(selectedPage).isEmpty()
                        ? BODY_TEXT_MAX_LINES_WITHOUT_RECIPE
                        : BODY_TEXT_MAX_LINES_WITH_RECIPE
        );

        int pageIndex = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage) + 1;
        Component indicator = Component.translatable(
                "dwm.guide.page.indicator",
                Component.translatable(selectedChapter.titleKey()),
                pageIndex,
                selectedChapter.pages().size()
        );
        graphics.text(
                font,
                indicator,
                rightX,
                bookTop + FieldGuideBookLayout.RIGHT_INDICATOR_Y,
                FieldGuideBookLayout.TEXT_COLOR,
                false
        );

        FieldGuideRecipePanel.render(
                graphics,
                minecraft,
                bookLeft,
                bookTop,
                selectedPage,
                selectedStation,
                mouseX,
                mouseY
        );

        if (selectedPage.patternPage()) {
            graphics.text(
                    font,
                    Component.translatable("dwm.guide.pattern.all_colours"),
                    rightX,
                    bookTop + FieldGuideBookLayout.RIGHT_PATTERN_Y,
                    0xFF666666,
                    false
            );
        }
    }

    private void renderWrappedBody(GuiGraphicsExtractor graphics, Component text, int x, int y, int maxWidth, int maxLines) {
        int lineCount = 0;
        for (var line : font.split(text, maxWidth)) {
            graphics.text(font, line, x, y, FieldGuideBookLayout.TEXT_COLOR, false);
            y += 9;
            lineCount++;
            if (lineCount >= maxLines) {
                break;
            }
        }
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
