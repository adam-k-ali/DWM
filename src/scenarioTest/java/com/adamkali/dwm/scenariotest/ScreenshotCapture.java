package com.adamkali.dwm.scenariotest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public final class ScreenshotCapture {
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
        if (completed.path() != null) {
            logger.info("captureScreenshot saved {}", completed.path());
        }
        return true;
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
