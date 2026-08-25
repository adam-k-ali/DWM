package com.adamkali.screenplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenshotCaptureTest {
    @Test
    void appendsPngWhenMissing() {
        assertEquals("after-world-tab.png", ScreenshotCapture.normalizeFileName("after-world-tab"));
    }

    @Test
    void keepsExistingPngSuffix() {
        assertEquals("after-world-tab.png", ScreenshotCapture.normalizeFileName("after-world-tab.png"));
    }

    @Test
    void rejectsBlankName() {
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> ScreenshotCapture.normalizeFileName("  ")
        );

        assertTrue(exception.getMessage().contains("non-empty string"));
    }

    @Test
    void rejectsPathSeparators() {
        ScenarioException slash = assertThrows(
                ScenarioException.class,
                () -> ScreenshotCapture.normalizeFileName("nested/shot.png")
        );
        ScenarioException parent = assertThrows(
                ScenarioException.class,
                () -> ScreenshotCapture.normalizeFileName("../escape.png")
        );

        assertTrue(slash.getMessage().contains("without path separators"));
        assertTrue(parent.getMessage().contains("without path separators"));
    }

    @Test
    void flagsTinyPngsAsSuspectBlankFrames() {
        assertTrue(ScreenshotCapture.isSuspectBlankSize(16_945L));
        assertTrue(ScreenshotCapture.isSuspectBlankSize(1L));
        assertFalse(ScreenshotCapture.isSuspectBlankSize(0L));
        assertFalse(ScreenshotCapture.isSuspectBlankSize(160_000L));
        assertFalse(ScreenshotCapture.isSuspectBlankSize(ScreenshotCapture.SUSPECT_BLANK_MAX_BYTES + 1));
    }
}
