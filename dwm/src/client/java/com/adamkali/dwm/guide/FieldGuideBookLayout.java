package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Layout constants aligned with vanilla {@link net.minecraft.client.gui.screens.inventory.BookViewScreen}.
 */
@Environment(EnvType.CLIENT)
public final class FieldGuideBookLayout {
    public static final int BOOK_WIDTH = 192;
    public static final int BOOK_HEIGHT = 192;

    public static final int LEFT_PAGE_X = 14;
    public static final int RIGHT_PAGE_X = 98;
    public static final int PAGE_TOP = 16;

    /** Writable width on the left index page (inside the parchment margin). */
    public static final int LEFT_PAGE_WIDTH = 76;
    /** Writable width on the right content page. */
    public static final int RIGHT_PAGE_WIDTH = 88;

    public static final int INDEX_HEADER_Y = PAGE_TOP;
    public static final int INDEX_CONTENT_Y = PAGE_TOP + 18;
    public static final int CHAPTER_ENTRY_HEIGHT = 10;
    public static final int PAGE_ENTRY_HEIGHT = 8;
    public static final int PAGE_ENTRY_INDENT = 6;

    public static final int RIGHT_TITLE_Y = PAGE_TOP;
    public static final int RIGHT_BODY_Y = PAGE_TOP + 14;
    public static final int RIGHT_RECIPE_Y = PAGE_TOP + 64;
    public static final int RIGHT_PATTERN_Y = 152;
    public static final int RIGHT_INDICATOR_Y = 168;

    public static final int PAGE_BACK_X = 43;
    public static final int PAGE_FORWARD_X = 116;
    public static final int PAGE_BUTTON_Y = 157;

    public static final int DONE_BUTTON_WIDTH = 200;
    public static final int DONE_BUTTON_Y_OFFSET = BOOK_HEIGHT + 4;

    public static final int TEXT_COLOR = 0xFF3F3F3F;
    public static final int TITLE_COLOR = 0xFF000000;
    public static final int INDEX_HEADER_COLOR = 0xFF2A2A2A;
    public static final int CHAPTER_SELECTED_COLOR = 0xFF000000;
    public static final int CHAPTER_UNSELECTED_COLOR = 0xFF707070;
    public static final int PAGE_SELECTED_COLOR = 0xFF1A3366;
    public static final int PAGE_UNSELECTED_COLOR = 0xFF505050;

    private FieldGuideBookLayout() {
    }

    public static int bookLeft(int screenWidth) {
        return screenWidth / 2 - BOOK_WIDTH / 2;
    }

    public static int bookTop(int screenHeight) {
        return (screenHeight - BOOK_HEIGHT) / 2;
    }

    public static int doneButtonTop(int bookTop) {
        return bookTop + DONE_BUTTON_Y_OFFSET;
    }
}
