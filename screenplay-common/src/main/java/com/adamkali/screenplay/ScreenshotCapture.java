package com.adamkali.screenplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public final class ScreenshotCapture {
    private static final int NO_DOWNSCALE = 1;
    /** Solid-black 854×480 PNGs from empty FBOs compress to ~17KB; real frames are much larger. */
    public static final long SUSPECT_BLANK_MAX_BYTES = 40_000L;
    private static final int MAX_BLANK_RETRIES = 45;

    private final Logger logger;
    private final AtomicReference<Object> inFlight = new AtomicReference<>();
    private int blankRetries;
    private int settleTicks;

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

    public static boolean isSuspectBlankSize(long byteLength) {
        return byteLength > 0L && byteLength < SUSPECT_BLANK_MAX_BYTES;
    }

    public boolean tick(Minecraft client, String filename) {
        Object current = inFlight.get();
        if (current == null) {
            if (settleTicks > 0) {
                settleTicks--;
                return false;
            }
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
            blankRetries = 0;
            settleTicks = 0;
            throw new ScenarioException("captureScreenshot failed: " + completed.message());
        }

        Path saved = resolveSavedPath(client, filename, completed.path());
        if (saved != null && isBlankPng(saved) && blankRetries < MAX_BLANK_RETRIES) {
            blankRetries++;
            settleTicks = 2;
            logger.warn(
                    "captureScreenshot {} looks blank ({}); retrying {}/{}",
                    saved.getFileName(),
                    sizeLabel(saved),
                    blankRetries,
                    MAX_BLANK_RETRIES
            );
            try {
                Files.deleteIfExists(saved);
            } catch (IOException ignored) {
                // Best-effort; next grab may overwrite.
            }
            return false;
        }
        if (saved != null && isBlankPng(saved)) {
            blankRetries = 0;
            settleTicks = 0;
            throw new ScenarioException(
                    "captureScreenshot produced a blank/black frame after "
                            + MAX_BLANK_RETRIES
                            + " retries: "
                            + saved
                            + " ("
                            + sizeLabel(saved)
                            + "). Client chunks may not have rendered; check launchGame finished LoadingOverlay.onFinish."
            );
        }

        blankRetries = 0;
        settleTicks = 0;
        if (completed.path() != null) {
            logger.info("captureScreenshot saved {}", completed.path());
        }
        return true;
    }

    private static Path resolveSavedPath(Minecraft client, String filename, String reportedPath) {
        if (reportedPath != null && !reportedPath.isBlank()) {
            return Path.of(reportedPath);
        }
        if (filename == null || filename.isBlank() || client.gameDirectory == null) {
            return null;
        }
        return client.gameDirectory.toPath().resolve("screenshots").resolve(filename);
    }

    private static boolean isBlankPng(Path path) {
        try {
            if (!Files.isRegularFile(path)) {
                return false;
            }
            return isSuspectBlankSize(Files.size(path));
        } catch (IOException ignored) {
            return false;
        }
    }

    private static String sizeLabel(Path path) {
        try {
            return Files.size(path) + " bytes";
        } catch (IOException exception) {
            return "unreadable";
        }
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
