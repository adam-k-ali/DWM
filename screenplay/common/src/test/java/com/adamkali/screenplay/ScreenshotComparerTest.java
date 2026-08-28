package com.adamkali.screenplay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenshotComparerTest {
    @TempDir
    Path temp;

    @Test
    void matchesIdenticalImages() throws Exception {
        Path a = fixture("identical-a.png");
        Path b = fixture("identical-b.png");

        ScreenshotComparer.Result result = ScreenshotComparer.compare(a, b, 0, temp.resolve("diff.png"));

        assertTrue(result.matched());
        assertEquals(0, result.diffPixels());
        assertEquals(4, result.width());
        assertEquals(4, result.height());
        assertNull(result.diffPath());
    }

    @Test
    void countsOnePixelDifferenceAndWritesDiff() throws Exception {
        Path actual = fixture("one-pixel-diff.png");
        Path baseline = fixture("identical-a.png");
        Path diff = temp.resolve("one-pixel-diff-diff.png");

        ScreenshotComparer.Result result = ScreenshotComparer.compare(actual, baseline, 0, diff);

        assertFalse(result.matched());
        assertEquals(1, result.diffPixels());
        assertEquals(diff, result.diffPath());
        assertTrue(Files.isRegularFile(diff));
        assertTrue(result.message().contains("mismatch"));
        assertTrue(result.message().contains("colorEpsilon=" + ScreenshotComparer.COLOR_EPSILON));
    }

    @Test
    void allowsDifferenceWithinMaxDiffPixels() throws Exception {
        Path actual = fixture("one-pixel-diff.png");
        Path baseline = fixture("identical-a.png");

        ScreenshotComparer.Result result = ScreenshotComparer.compare(actual, baseline, 1, temp.resolve("diff.png"));

        assertTrue(result.matched());
        assertEquals(1, result.diffPixels());
        assertNull(result.diffPath());
    }

    @Test
    void ignoresSoftChannelNoiseWithinColorEpsilon() throws Exception {
        Path baseline = writeSolid(temp.resolve("baseline.png"), 0xFF102030);
        Path actual = writeSolid(temp.resolve("actual.png"), 0xFF102030 + ScreenshotComparer.COLOR_EPSILON);

        ScreenshotComparer.Result result = ScreenshotComparer.compare(actual, baseline, 0, temp.resolve("diff.png"));

        assertTrue(result.matched());
        assertEquals(0, result.diffPixels());
        assertNull(result.diffPath());
    }

    @Test
    void countsPixelsBeyondColorEpsilon() throws Exception {
        Path baseline = writeSolid(temp.resolve("baseline.png"), 0xFF102030);
        Path actual = writeSolid(
                temp.resolve("actual.png"),
                0xFF102030 + ScreenshotComparer.COLOR_EPSILON + 1
        );
        Path diff = temp.resolve("beyond-epsilon-diff.png");

        ScreenshotComparer.Result result = ScreenshotComparer.compare(actual, baseline, 0, diff);

        assertFalse(result.matched());
        assertEquals(16, result.diffPixels());
        assertEquals(diff, result.diffPath());
    }

    @Test
    void withinColorEpsilonHelper() {
        assertTrue(ScreenshotComparer.withinColorEpsilon(0xFF010203, 0xFF010203));
        assertTrue(ScreenshotComparer.withinColorEpsilon(
                0xFF000000,
                0xFF000000 | ScreenshotComparer.COLOR_EPSILON
        ));
        assertFalse(ScreenshotComparer.withinColorEpsilon(
                0xFF000000,
                0xFF000000 | (ScreenshotComparer.COLOR_EPSILON + 1)
        ));
    }

    @Test
    void reportsDimensionMismatch() throws Exception {
        Path actual = fixture("size-mismatch.png");
        Path baseline = fixture("identical-a.png");

        ScreenshotComparer.Result result = ScreenshotComparer.compare(actual, baseline, 0, temp.resolve("diff.png"));

        assertFalse(result.matched());
        assertTrue(result.message().contains("dimension mismatch"));
        assertNull(result.diffPath());
    }

    @Test
    void reportsMissingBaseline() throws Exception {
        Path actual = fixture("identical-a.png");
        Path baseline = temp.resolve("missing.png");

        ScreenshotComparer.Result result = ScreenshotComparer.compare(actual, baseline, 0, null);

        assertFalse(result.matched());
        assertTrue(result.message().contains("baseline not found"));
    }

    private static Path fixture(String name) throws URISyntaxException {
        Path path = Path.of(ScreenshotComparerTest.class.getResource("/screenshot-fixtures/" + name).toURI());
        assertNotNull(path);
        return path;
    }

    private static Path writeSolid(Path path, int argb) throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                image.setRGB(x, y, argb);
            }
        }
        ImageIO.write(image, "png", path.toFile());
        return path;
    }
}
