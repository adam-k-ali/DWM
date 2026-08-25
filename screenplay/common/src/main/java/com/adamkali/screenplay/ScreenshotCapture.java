package com.adamkali.screenplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public final class ScreenshotCapture {
    public static final String BASELINES_DIR_PROPERTY = "screenplay.baselines-dir";

    private static final int NO_DOWNSCALE = 1;

    private final Logger logger;
    private final AtomicReference<Object> inFlight = new AtomicReference<>();

    ScreenshotCapture(Logger logger) {
        this.logger = logger;
    }

    public static String normalizeFileName(String name) {
        if (name == null || name.isBlank()) {
            throw new ScenarioException("captureScreenshot name must be a non-empty string");
        }
        String trimmed = name.trim();
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            throw new ScenarioException(
                    "captureScreenshot name must be a file name without path separators: '" + name + "'"
            );
        }
        if (trimmed.toLowerCase(Locale.ROOT).endsWith(".png")) {
            return trimmed;
        }
        return trimmed + ".png";
    }

    public boolean tick(Minecraft client, String filename) {
        return tick(client, filename, false, 0L);
    }

    public boolean tick(Minecraft client, String filename, boolean compare, long maxDiffPixels) {
        Object current = inFlight.get();
        if (current == null) {
            inFlight.set(Pending.INSTANCE);
            try {
                Screenshot.grab(
                        client.gameDirectory,
                        filename,
                        client.gameRenderer.mainRenderTarget(),
                        NO_DOWNSCALE,
                        message -> inFlight.set(interpret(message))
                );
            } catch (RuntimeException exception) {
                inFlight.set(null);
                throw exception;
            }
            return false;
        }
        if (current == Pending.INSTANCE) {
            return false;
        }

        Completed completed = (Completed) current;
        inFlight.set(null);
        logger.info("{}", completed.message());
        if (completed.failed()) {
            throw new ScenarioException("captureScreenshot failed: " + completed.message());
        }
        Path actualPath = resolveActualPath(client, filename, completed.path());
        if (completed.path() != null) {
            logger.info("captureScreenshot saved {}", completed.path());
        } else if (actualPath != null) {
            logger.info("captureScreenshot saved {}", actualPath);
        }
        if (compare) {
            compareToBaseline(actualPath, filename, maxDiffPixels);
        }
        return true;
    }

    private void compareToBaseline(Path actualPath, String filename, long maxDiffPixels) {
        String baselinesDir = System.getProperty(BASELINES_DIR_PROPERTY);
        if (baselinesDir == null || baselinesDir.isBlank()) {
            logger.info("captureScreenshot compare skipped: system property '{}' is unset", BASELINES_DIR_PROPERTY);
            return;
        }
        if (filename == null || filename.isBlank()) {
            throw new ScenarioException("captureScreenshot compare requires a non-empty string 'name'");
        }
        if (actualPath == null || !Files.isRegularFile(actualPath)) {
            throw new ScenarioException("captureScreenshot compare could not locate saved PNG for '" + filename + "'");
        }

        Path baseline = Path.of(baselinesDir).resolve(filename);
        if (!Files.isRegularFile(baseline)) {
            logger.info("captureScreenshot compare NO BASELINE for {} under {}", filename, baselinesDir);
            return;
        }

        String stem = filename.toLowerCase(Locale.ROOT).endsWith(".png")
                ? filename.substring(0, filename.length() - 4)
                : filename;
        Path diffPath = actualPath.resolveSibling(stem + "-diff.png");
        ScreenshotComparer.Result result = ScreenshotComparer.compare(actualPath, baseline, maxDiffPixels, diffPath);
        if (result.matched()) {
            logger.info("captureScreenshot compare OK for {} ({})", filename, result.message());
            return;
        }
        String detail = result.message();
        if (result.diffPath() != null) {
            detail = detail + "; diff written to " + result.diffPath();
        }
        throw new ScenarioException("captureScreenshot compare failed for '" + filename + "': " + detail
                + " (actual=" + actualPath + ", baseline=" + baseline + ")");
    }

    private static Path resolveActualPath(Minecraft client, String filename, String reportedPath) {
        if (reportedPath != null && !reportedPath.isBlank()) {
            Path reported = Path.of(reportedPath);
            if (Files.isRegularFile(reported)) {
                return reported;
            }
        }
        if (filename == null || filename.isBlank() || client.gameDirectory == null) {
            return null;
        }
        return client.gameDirectory.toPath().resolve("screenshots").resolve(filename);
    }

    private static Completed interpret(Component message) {
        String text = message.getString();
        if (message.getContents() instanceof TranslatableContents contents
                && "screenshot.failure".equals(contents.getKey())) {
            Object[] args = contents.getArgs();
            String detail = args.length > 0 ? String.valueOf(args[0]) : text;
            return new Completed(true, detail, null);
        }
        return new Completed(false, text, savedPath(message));
    }

    private static String savedPath(Component message) {
        if (message.getContents() instanceof TranslatableContents contents) {
            for (Object arg : contents.getArgs()) {
                if (arg instanceof Component component) {
                    String path = pathFromClick(component);
                    if (path != null) {
                        return path;
                    }
                }
            }
        }
        return pathFromClick(message);
    }

    private static String pathFromClick(Component component) {
        if (component.getStyle().getClickEvent() instanceof ClickEvent.OpenFile openFile) {
            return openFile.file().getAbsolutePath();
        }
        for (Component sibling : component.getSiblings()) {
            String path = pathFromClick(sibling);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private enum Pending {
        INSTANCE
    }

    private record Completed(boolean failed, String message, String path) {
    }
}
