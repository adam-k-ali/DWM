package com.adamkali.dwm.render;

import com.adamkali.dwm.item.SonicFieldMode;

/**
 * Pure pixel geometry for the field-mode carousel and its held-item indicator.
 */
public final class SonicCarouselLayout {
    public static final int BASE_SLOT_SIZE = 32;
    public static final int BASE_ICON_SIZE = 16;
    public static final int SLOT_SPACING = 32;
    public static final int INDICATOR_WIDTH = 108;
    public static final int INDICATOR_HEIGHT = 30;
    public static final int INDICATOR_MARGIN = 8;

    private static final float SCALE_NEAR = 0.78f;
    private static final float SCALE_FAR = 0.60f;
    private static final float SCALE_EXIT = 0.45f;

    private SonicCarouselLayout() {
    }

    public static float visualOffset(
            SonicFieldMode preview,
            SonicFieldMode mode,
            float visualScroll,
            float targetScroll
    ) {
        float unwrappedIndex = targetScroll + SonicFieldMode.signedOffset(preview, mode);
        if (mode != preview) {
            int modeCount = SonicFieldMode.cycleOrder().length;
            unwrappedIndex += Math.round((visualScroll - unwrappedIndex) / modeCount) * modeCount;
        }
        return unwrappedIndex - visualScroll;
    }

    public static SlotGeometry slot(float offset, int centerX, int rowY, float selectedScaleBoost) {
        float scale = scaleForOffset(offset) + selectedScaleBoost;
        int slotSize = scaledSize(BASE_SLOT_SIZE, scale);
        int iconSize = scaledSize(BASE_ICON_SIZE, scale);
        int slotCenterX = centerX + Math.round(offset * SLOT_SPACING);
        int slotX = slotCenterX - slotSize / 2;
        int slotY = rowY + (BASE_SLOT_SIZE - slotSize) / 2;
        int iconX = slotCenterX - iconSize / 2;
        int iconY = rowY + BASE_SLOT_SIZE / 2 - iconSize / 2;
        return new SlotGeometry(slotX, slotY, slotSize, iconX, iconY, iconSize);
    }

    public static float scaleForOffset(float offset) {
        float distance = Math.abs(offset);
        if (distance <= 1.0f) {
            return lerp(1.0f, SCALE_NEAR, distance);
        }
        if (distance <= 2.0f) {
            return lerp(SCALE_NEAR, SCALE_FAR, distance - 1.0f);
        }
        return lerp(SCALE_FAR, SCALE_EXIT, Math.min(1.0f, distance - 2.0f));
    }

    public static int indicatorX(int guiWidth) {
        return Math.max(INDICATOR_MARGIN, guiWidth - INDICATOR_MARGIN - INDICATOR_WIDTH);
    }

    public static int indicatorY(boolean statusEffectsVisible) {
        return statusEffectsVisible ? 34 : INDICATOR_MARGIN;
    }

    public static boolean shouldShowIndicator(boolean holdingSonic, boolean carouselActive) {
        return holdingSonic && !carouselActive;
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static int scaledSize(int base, float scale) {
        return Math.max(1, Math.round(base * scale));
    }

    public record SlotGeometry(
            int slotX,
            int slotY,
            int slotSize,
            int iconX,
            int iconY,
            int iconSize
    ) {
    }
}
