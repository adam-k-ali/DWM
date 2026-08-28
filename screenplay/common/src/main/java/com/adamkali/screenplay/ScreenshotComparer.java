package com.adamkali.screenplay;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PNG comparison for Screenplay visual regression.
 *
 * <p>Pixels whose per-channel ARGB deltas are all within {@link #COLOR_EPSILON}
 * count as equal. That absorbs soft framebuffer noise from CI software GL
 * (xvfb/llvmpipe) without adding YAML knobs. Structural changes still register
 * as differing pixels and are gated by {@code maxDiffPixels}.
 */
public final class ScreenshotComparer {
    /**
     * Max absolute per-channel (A/R/G/B) difference still treated as equal.
     * Chosen to cover typical llvmpipe soft variance (grass AA / silhouette edges)
     * while keeping large UI / lighting / layout changes visible to {@code maxDiffPixels}.
     */
    public static final int COLOR_EPSILON = 24;

    private static final int DIFF_RGB = 0xFFFF0000;

    private ScreenshotComparer() {
    }

    public record Result(
            boolean matched,
            long diffPixels,
            int width,
            int height,
            Path diffPath,
            String message
    ) {
    }

    public static Result compare(Path actual, Path baseline, long maxDiffPixels, Path diffOutput) {
        if (actual == null || baseline == null) {
            throw new IllegalArgumentException("actual and baseline paths are required");
        }
        if (maxDiffPixels < 0) {
            throw new IllegalArgumentException("maxDiffPixels must be >= 0");
        }
        if (!Files.isRegularFile(baseline)) {
            return new Result(false, -1, 0, 0, null, "baseline not found: " + baseline);
        }
        if (!Files.isRegularFile(actual)) {
            return new Result(false, -1, 0, 0, null, "actual screenshot not found: " + actual);
        }

        BufferedImage actualImage;
        BufferedImage baselineImage;
        try {
            actualImage = ImageIO.read(actual.toFile());
            baselineImage = ImageIO.read(baseline.toFile());
        } catch (IOException exception) {
            return new Result(false, -1, 0, 0, null, "failed to read PNG: " + exception.getMessage());
        }
        if (actualImage == null) {
            return new Result(false, -1, 0, 0, null, "unreadable actual PNG: " + actual);
        }
        if (baselineImage == null) {
            return new Result(false, -1, 0, 0, null, "unreadable baseline PNG: " + baseline);
        }

        int width = actualImage.getWidth();
        int height = actualImage.getHeight();
        if (width != baselineImage.getWidth() || height != baselineImage.getHeight()) {
            return new Result(
                    false,
                    -1,
                    width,
                    height,
                    null,
                    "dimension mismatch: actual=" + width + "x" + height
                            + " baseline=" + baselineImage.getWidth() + "x" + baselineImage.getHeight()
            );
        }

        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        long diffPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int actualArgb = actualImage.getRGB(x, y);
                int baselineArgb = baselineImage.getRGB(x, y);
                if (withinColorEpsilon(actualArgb, baselineArgb)) {
                    diffImage.setRGB(x, y, actualArgb);
                } else {
                    diffPixels++;
                    diffImage.setRGB(x, y, DIFF_RGB);
                }
            }
        }

        if (diffPixels <= maxDiffPixels) {
            return new Result(true, diffPixels, width, height, null,
                    "matched (" + diffPixels + " differing pixels, max " + maxDiffPixels
                            + ", colorEpsilon=" + COLOR_EPSILON + ")");
        }

        Path writtenDiff = null;
        if (diffOutput != null) {
            try {
                Path parent = diffOutput.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                if (!ImageIO.write(diffImage, "png", diffOutput.toFile())) {
                    return new Result(false, diffPixels, width, height, null,
                            "mismatch (" + diffPixels + " differing pixels, max " + maxDiffPixels
                                    + ", colorEpsilon=" + COLOR_EPSILON + ") and failed to write diff PNG");
                }
                writtenDiff = diffOutput;
            } catch (IOException exception) {
                return new Result(false, diffPixels, width, height, null,
                        "mismatch (" + diffPixels + " differing pixels, max " + maxDiffPixels
                                + ", colorEpsilon=" + COLOR_EPSILON
                                + ") and failed to write diff PNG: " + exception.getMessage());
            }
        }
        return new Result(false, diffPixels, width, height, writtenDiff,
                "mismatch (" + diffPixels + " differing pixels, max " + maxDiffPixels
                        + ", colorEpsilon=" + COLOR_EPSILON + ")");
    }

    static boolean withinColorEpsilon(int actualArgb, int baselineArgb) {
        if (actualArgb == baselineArgb) {
            return true;
        }
        return channelDelta(actualArgb, baselineArgb, 24) <= COLOR_EPSILON
                && channelDelta(actualArgb, baselineArgb, 16) <= COLOR_EPSILON
                && channelDelta(actualArgb, baselineArgb, 8) <= COLOR_EPSILON
                && channelDelta(actualArgb, baselineArgb, 0) <= COLOR_EPSILON;
    }

    private static int channelDelta(int actualArgb, int baselineArgb, int shift) {
        return Math.abs(((actualArgb >> shift) & 0xFF) - ((baselineArgb >> shift) & 0xFF));
    }
}
