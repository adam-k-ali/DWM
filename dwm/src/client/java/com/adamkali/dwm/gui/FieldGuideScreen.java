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
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FieldGuideScreen extends Screen {
    private static final int BODY_TEXT_MAX_LINES = 5;

    private final List<Button> chapterButtons = new ArrayList<>();
    private final List<Button> pageButtons = new ArrayList<>();
    private final List<Button> stationTabButtons = new ArrayList<>();
    private Button previousPageButton;
    private Button nextPageButton;
    private Button doneButton;

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
        bookLeft = width / 2 - FieldGuideBookLayout.BOOK_WIDTH / 2;
        bookTop = height / 2 - FieldGuideBookLayout.BOOK_HEIGHT / 2;

        clearIndexWidgets();
        rebuildIndexWidgets();

        if (selectedPage == null) {
            selectedChapter = FieldGuideCatalog.chapters().getFirst();
            selectedPage = selectedChapter.pages().getFirst();
        } else if (selectedChapter == null) {
            selectedChapter = FieldGuideCatalog.chapterForPage(selectedPage);
        }

        int navY = bookTop + FieldGuideBookLayout.BOOK_HEIGHT - FieldGuideBookLayout.scale(28);
        int navX = bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X;
        previousPageButton = Button.builder(Component.translatable("dwm.guide.page.previous"), b -> turnPage(-1))
                .bounds(navX, navY, FieldGuideBookLayout.scale(62), 18)
                .build();
        nextPageButton = Button.builder(Component.translatable("dwm.guide.page.next"), b -> turnPage(1))
                .bounds(navX + FieldGuideBookLayout.scale(66), navY, FieldGuideBookLayout.scale(62), 18)
                .build();
        doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(bookLeft + FieldGuideBookLayout.BOOK_WIDTH - FieldGuideBookLayout.scale(56),
                        bookTop + FieldGuideBookLayout.BOOK_HEIGHT - FieldGuideBookLayout.scale(18),
                        FieldGuideBookLayout.scale(48),
                        18)
                .build();

        addRenderableWidget(previousPageButton);
        addRenderableWidget(nextPageButton);
        addRenderableWidget(doneButton);

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
        int chapterY = bookTop + FieldGuideBookLayout.PAGE_TOP;
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
            FieldGuideChapter targetChapter = chapter;
            Button chapterButton = Button.builder(Component.translatable(chapter.titleKey()), b -> selectChapter(targetChapter))
                    .bounds(bookLeft + FieldGuideBookLayout.LEFT_PAGE_X,
                            chapterY,
                            FieldGuideBookLayout.INDEX_BUTTON_WIDTH,
                            FieldGuideBookLayout.CHAPTER_BUTTON_HEIGHT)
                    .build();
            addRenderableWidget(chapterButton);
            chapterButtons.add(chapterButton);
            chapterY += FieldGuideBookLayout.CHAPTER_BUTTON_HEIGHT + 2;
        }

        if (selectedChapter != null) {
            int pageY = chapterY + 4;
            for (FieldGuidePage page : selectedChapter.pages()) {
                FieldGuidePage targetPage = page;
                Button pageButton = Button.builder(Component.translatable(page.titleKey()), b -> selectPage(targetPage))
                        .bounds(bookLeft + FieldGuideBookLayout.LEFT_PAGE_X + 6,
                                pageY,
                                FieldGuideBookLayout.INDEX_BUTTON_WIDTH - 6,
                                FieldGuideBookLayout.INDEX_BUTTON_HEIGHT)
                        .build();
                addRenderableWidget(pageButton);
                pageButtons.add(pageButton);
                pageY += FieldGuideBookLayout.INDEX_BUTTON_HEIGHT + 1;
            }
        }
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
        init();
    }

    private void updateNavigationState() {
        if (selectedChapter == null || selectedPage == null || previousPageButton == null || nextPageButton == null) {
            return;
        }
        int index = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage);
        previousPageButton.active = index > 0;
        nextPageButton.active = index < selectedChapter.pages().size() - 1;
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
        int tabY = bookTop + FieldGuideBookLayout.BOOK_HEIGHT - FieldGuideBookLayout.scale(48);
        for (FieldGuideRecipePanel.Station station : stations) {
            FieldGuideRecipePanel.Station target = station;
            Button tab = Button.builder(station.label(), b -> {
                selectedStation = target;
                rebuildStationTabs();
            }).bounds(tabX, tabY, FieldGuideBookLayout.scale(52), 16).build();
            tab.active = station != selectedStation;
            addRenderableWidget(tab);
            stationTabButtons.add(tab);
            tabX += FieldGuideBookLayout.scale(54);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        renderBookBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (selectedChapter == null || selectedPage == null) {
            return;
        }

        int rightX = bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X;
        int rightY = bookTop + FieldGuideBookLayout.PAGE_TOP;

        graphics.text(font, Component.translatable(selectedPage.titleKey()), rightX, rightY, FieldGuideBookLayout.TITLE_COLOR, false);
        renderWrappedBody(graphics, Component.translatable(selectedPage.bodyKey()), rightX, rightY + 12, FieldGuideBookLayout.RIGHT_PAGE_WIDTH);

        int pageIndex = FieldGuideCatalog.pageIndexInChapter(selectedChapter, selectedPage) + 1;
        Component indicator = Component.translatable(
                "dwm.guide.page.indicator",
                Component.translatable(selectedChapter.titleKey()),
                pageIndex,
                selectedChapter.pages().size()
        );
        graphics.text(font, indicator, rightX, bookTop + FieldGuideBookLayout.BOOK_HEIGHT - FieldGuideBookLayout.scale(62),
                FieldGuideBookLayout.TEXT_COLOR, false);

        FieldGuideRecipePanel.render(
                graphics,
                minecraft,
                rightX,
                rightY + FieldGuideBookLayout.scale(58),
                selectedPage,
                selectedStation,
                mouseX,
                mouseY
        );

        highlightSelectedIndex(graphics);
    }

    private void renderBookBackground(GuiGraphicsExtractor graphics) {
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

    private void highlightSelectedIndex(GuiGraphicsExtractor graphics) {
        if (selectedChapter != null) {
            highlightButton(graphics, chapterButtons, selectedChapter.titleKey(), FieldGuideBookLayout.CHAPTER_SELECTED_COLOR);
        }
        if (selectedPage != null) {
            highlightButton(graphics, pageButtons, selectedPage.titleKey(), FieldGuideBookLayout.PAGE_SELECTED_COLOR);
        }
    }

    private void highlightButton(GuiGraphicsExtractor graphics, List<Button> buttons, String titleKey, int color) {
        for (Button button : buttons) {
            if (titleKey.equals(titleKey(button))) {
                int x = button.getX();
                int y = button.getY();
                graphics.fill(x - 1, y - 1, x + button.getWidth() + 1, y + button.getHeight() + 1, color | 0x40000000);
            }
        }
    }

    private static @Nullable String titleKey(Button button) {
        if (button.getMessage().getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        return null;
    }

    private void renderWrappedBody(GuiGraphicsExtractor graphics, Component text, int x, int y, int maxWidth) {
        int lineCount = 0;
        for (var line : font.split(text, maxWidth)) {
            graphics.text(font, line, x, y, FieldGuideBookLayout.TEXT_COLOR, false);
            y += 10;
            lineCount++;
            if (lineCount >= BODY_TEXT_MAX_LINES) {
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
