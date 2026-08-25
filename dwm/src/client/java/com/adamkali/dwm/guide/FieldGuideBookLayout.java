package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class FieldGuideBookLayout {
    public static final int SCALE_NUMERATOR = 5;
    public static final int SCALE_DENOMINATOR = 4;
    public static final int BOOK_WIDTH = 192 * SCALE_NUMERATOR / SCALE_DENOMINATOR;
    public static final int BOOK_HEIGHT = 192 * SCALE_NUMERATOR / SCALE_DENOMINATOR;

    public static final int LEFT_PAGE_X = 15;
    public static final int RIGHT_PAGE_X = 98;
    public static final int PAGE_TOP = 18;
    public static final int LEFT_PAGE_WIDTH = 88;
    public static final int RIGHT_PAGE_WIDTH = 114;
    public static final int INDEX_BUTTON_WIDTH = 82;
    public static final int INDEX_BUTTON_HEIGHT = 16;
    public static final int CHAPTER_BUTTON_HEIGHT = 18;

    public static final int TEXT_COLOR = 0xFF3F3F3F;
    public static final int TITLE_COLOR = 0xFF000000;
    public static final int CHAPTER_SELECTED_COLOR = 0xFF3355AA;
    public static final int PAGE_SELECTED_COLOR = 0xFF809CCC;

    private FieldGuideBookLayout() {
    }

    public static int scale(int value) {
        return value * SCALE_NUMERATOR / SCALE_DENOMINATOR;
    }
}
