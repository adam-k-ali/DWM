package com.adamkali.dwm.gui;

import com.adamkali.dwm.guide.FieldGuideCatalog;
import com.adamkali.dwm.guide.FieldGuideChapter;
import com.adamkali.dwm.guide.FieldGuidePage;
import com.adamkali.dwm.guide.FieldGuideRecipePanel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FieldGuideScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 240;
    private static final int LIST_WIDTH = 118;
    private static final int CONTENT_LEFT_OFFSET = 126;
    private static final int CONTENT_WIDTH = 180;
    private static final int BODY_TOP_OFFSET = 36;
    private static final int BODY_HEIGHT = 168;
    private static final int PAGE_BUTTON_HEIGHT = 18;

    private final List<ChapterMarker> chapterMarkers = new ArrayList<>();
    private final List<Button> pageButtons = new ArrayList<>();
    private final List<Button> stationTabButtons = new ArrayList<>();
    private Button doneButton;
    private @Nullable FieldGuidePage selectedPage;
    private FieldGuideRecipePanel.Station selectedStation = FieldGuideRecipePanel.Station.CRAFTING;
    private int panelLeft;
    private int panelTop;

    public FieldGuideScreen() {
        super(Component.translatable("dwm.guide.title"));
    }

    public FieldGuideScreen(@Nullable FieldGuidePage initialPage) {
        super(Component.translatable("dwm.guide.title"));
        this.selectedPage = initialPage;
    }

    @Override
    protected void init() {
        panelLeft = width / 2 - PANEL_WIDTH / 2;
        panelTop = height / 2 - PANEL_HEIGHT / 2;

        chapterMarkers.clear();
        pageButtons.forEach(this::removeWidget);
        pageButtons.clear();
        stationTabButtons.forEach(this::removeWidget);
        stationTabButtons.clear();

        int navY = panelTop + BODY_TOP_OFFSET;
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
            chapterMarkers.add(new ChapterMarker(chapter.titleKey(), navY));
            navY += 12;
            for (FieldGuidePage page : chapter.pages()) {
                FieldGuidePage target = page;
                Button pageButton = Button.builder(Component.translatable(page.titleKey()), b -> selectPage(target))
                        .bounds(panelLeft + 8, navY, LIST_WIDTH, PAGE_BUTTON_HEIGHT)
                        .build();
                addRenderableWidget(pageButton);
                pageButtons.add(pageButton);
                navY += PAGE_BUTTON_HEIGHT + 2;
            }
            navY += 4;
        }

        if (selectedPage == null) {
            selectedPage = firstPage();
        }
        selectPage(selectedPage);

        doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(panelLeft + PANEL_WIDTH - 78, panelTop + PANEL_HEIGHT - 24, 70, 20)
                .build();
        addRenderableWidget(doneButton);

        rebuildStationTabs();
    }

    private @Nullable FieldGuidePage firstPage() {
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
            if (!chapter.pages().isEmpty()) {
                return chapter.pages().getFirst();
            }
        }
        return null;
    }

    private void selectPage(FieldGuidePage page) {
        selectedPage = page;
        List<FieldGuideRecipePanel.Station> stations = FieldGuideRecipePanel.availableStations(page);
        if (!stations.isEmpty() && !stations.contains(selectedStation)) {
            selectedStation = stations.getFirst();
        }
        rebuildStationTabs();
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
        int tabX = panelLeft + CONTENT_LEFT_OFFSET;
        int tabY = panelTop + BODY_TOP_OFFSET + BODY_HEIGHT - 18;
        for (FieldGuideRecipePanel.Station station : stations) {
            FieldGuideRecipePanel.Station target = station;
            Button tab = Button.builder(station.label(), b -> {
                selectedStation = target;
                rebuildStationTabs();
            }).bounds(tabX, tabY, 56, 16).build();
            tab.active = station != selectedStation;
            addRenderableWidget(tab);
            stationTabButtons.add(tab);
            tabX += 58;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(font, getTitle(), panelLeft + 10, panelTop + 10, 0xFFFFFFFF, false);

        for (ChapterMarker marker : chapterMarkers) {
            graphics.text(font, Component.translatable(marker.titleKey()), panelLeft + 8, marker.y(), 0xFFD4A84B, false);
        }

        if (selectedPage == null) {
            return;
        }

        int contentLeft = panelLeft + CONTENT_LEFT_OFFSET;
        int contentTop = panelTop + BODY_TOP_OFFSET;
        graphics.fill(contentLeft, contentTop, contentLeft + CONTENT_WIDTH, contentTop + BODY_HEIGHT, 0xFF333333);

        graphics.text(font, Component.translatable(selectedPage.titleKey()), contentLeft + 6, contentTop + 6, 0xFFFFFFFF, false);
        renderWrappedBody(graphics, Component.translatable(selectedPage.bodyKey()), contentLeft + 6, contentTop + 20, CONTENT_WIDTH - 12);

        FieldGuideRecipePanel.render(
                graphics,
                minecraft,
                contentLeft + 6,
                contentTop + 72,
                selectedPage,
                selectedStation
        );

        String selectedKey = selectedPage.titleKey();
        for (Button button : pageButtons) {
            if (selectedKey.equals(titleKey(button))) {
                int x = button.getX();
                int y = button.getY();
                graphics.fill(x - 1, y - 1, x + button.getWidth() + 1, y + button.getHeight() + 1, 0x809CCC6A);
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
        for (var line : font.split(text, maxWidth)) {
            graphics.text(font, line, x, y, 0xFFCCCCCC, false);
            y += 10;
            if (y > panelTop + BODY_TOP_OFFSET + 64) {
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

    private record ChapterMarker(String titleKey, int y) {
    }
}
