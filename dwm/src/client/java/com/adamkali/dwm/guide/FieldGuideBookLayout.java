package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Layout constants for the Field Guide's two-page catalog.
 */
@Environment(EnvType.CLIENT)
public final class FieldGuideBookLayout {
    public static final int BOOK_WIDTH = 390;
    public static final int BOOK_HEIGHT = 220;

    public static final int LEFT_PAGE_X = 16;
    public static final int LEFT_PAGE_WIDTH = 158;
    public static final int RIGHT_PAGE_X = 207;
    public static final int RIGHT_PAGE_WIDTH = 166;
    public static final int GUTTER_X = 190;
    public static final int GUTTER_WIDTH = 10;

    public static final int HEADER_Y = 14;
    public static final int HEADER_HEIGHT = 22;
    public static final int INDEX_HEADER_Y = 44;
    public static final int INDEX_CONTENT_Y = 59;
    public static final int CHAPTER_ENTRY_HEIGHT = 14;
    public static final int PAGE_HEADER_Y = 108;
    public static final int PAGE_CONTENT_Y = 122;
    public static final int PAGE_ENTRY_HEIGHT = 11;
    public static final int PAGE_ENTRY_INDENT = 8;

    public static final int RIGHT_CHAPTER_Y = 20;
    public static final int RIGHT_TITLE_Y = 37;
    public static final int RIGHT_BODY_Y = 56;
    public static final int RIGHT_RECIPE_LABEL_Y = 94;
    public static final int RIGHT_RECIPE_Y = 108;
    public static final int VARIANT_SLOT_SIZE = 18;
    public static final int VARIANT_ICON_PAD = 1;
    public static final int VARIANT_ICON_GAP = 3;
    public static final int RIGHT_PATTERN_Y = 177;
    public static final int RIGHT_INDICATOR_Y = 203;

    public static final int PAGE_BACK_X = RIGHT_PAGE_X + 3;
    public static final int PAGE_FORWARD_X = RIGHT_PAGE_X + RIGHT_PAGE_WIDTH - 26;
    public static final int PAGE_BUTTON_Y = 194;

    public static final int DONE_BUTTON_WIDTH = 200;
    public static final int DONE_BUTTON_Y_OFFSET = BOOK_HEIGHT + 6;

    public static final int COVER_COLOR = 0xFF5A3928;
    public static final int COVER_EDGE_COLOR = 0xFF2F2018;
    public static final int PAGE_COLOR = 0xFFF2E5C4;
    public static final int PAGE_INSET_COLOR = 0xFFE8D5A8;
    public static final int GUTTER_COLOR = 0xFF7B573D;
    public static final int HEADER_COLOR = 0xFF273C59;
    public static final int ACCENT_COLOR = 0xFFC79A45;
    public static final int TEXT_COLOR = 0xFF342D25;
    public static final int MUTED_TEXT_COLOR = 0xFF756A5A;
    public static final int TITLE_COLOR = 0xFF1D2735;
    public static final int INDEX_HEADER_COLOR = 0xFF614A2C;
    public static final int CHAPTER_SELECTED_COLOR = 0xFF213B61;
    public static final int CHAPTER_UNSELECTED_COLOR = 0xFF6F6556;
    public static final int PAGE_SELECTED_COLOR = 0xFF213B61;
    public static final int PAGE_UNSELECTED_COLOR = 0xFF51493E;

    private FieldGuideBookLayout() {
    }

    public static int bookLeft(int screenWidth) {
        return screenWidth / 2 - BOOK_WIDTH / 2;
    }

    public static int bookTop(int screenHeight) {
        return Math.max(8, (screenHeight - BOOK_HEIGHT - 26) / 2);
    }

    public static int doneButtonTop(int bookTop) {
        return bookTop + DONE_BUTTON_Y_OFFSET;
    }
}
