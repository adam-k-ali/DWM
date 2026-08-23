package com.adamkali.screenplay.primitive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.ReloadInstance;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Waits for the title screen after resource reload.
 * <p>
 * Under Forge/NeoForge + xvfb, Minecraft client ticks can stall while render frames still
 * run, so {@link LoadingOverlay#tick()} (which invokes {@code onFinish}) may never run on
 * its own. Screenplay therefore drives {@code tick()} from the scenario loop when reload is
 * done. Clearing the overlay without {@code onFinish} leaves the client half-initialized and
 * causes later chunk-load timeouts with solid-black screenshots.
 */
public final class LaunchGamePrimitive extends NoArgPrimitive {
    private static Field loadingOverlayReloadField;
    private static boolean loadingOverlayReloadFieldResolved;
    private static Field loadingOverlayFadeOutStartField;
    private static boolean loadingOverlayFadeOutStartFieldResolved;
    private static Field loadingOverlayOnFinishField;
    private static boolean loadingOverlayOnFinishFieldResolved;
    private static long lastReloadLogNanos;

    @Override
    public String name() {
        return "launchGame";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireNoArguments(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Minecraft client = context.client();
        if (client == null || client.gui == null) {
            return false;
        }

        Overlay overlay = client.gui.overlay();
        maybeLogReloadProgress(context, overlay);

        if (overlay instanceof LoadingOverlay loadingOverlay && isReloadDone(overlay)) {
            // Prefer the real finish path so Minecraft's onFinish callback runs.
            loadingOverlay.tick();
            if (hasFadeOutStarted(loadingOverlay) || ensureOnFinishInvoked(loadingOverlay)) {
                client.gui.setOverlay(null);
            }
        }

        if (context.screen() instanceof AccessibilityOnboardingScreen) {
            client.gui.setScreen(new TitleScreen());
            return false;
        }
        return context.screen() instanceof TitleScreen && client.gui.overlay() == null;
    }

    private static void maybeLogReloadProgress(ScenarioPrimitiveContext context, Overlay overlay) {
        long now = System.nanoTime();
        if (lastReloadLogNanos != 0L && now - lastReloadLogNanos < 5_000_000_000L) {
            return;
        }
        lastReloadLogNanos = now;
        if (overlay == null) {
            context.logger().info("launchGame: overlay=<none> screen={}",
                    context.screen() == null ? "<none>" : context.screen().getClass().getName());
            return;
        }
        try {
            Field field = resolveReloadField(overlay);
            if (field == null) {
                context.logger().info("launchGame: overlay={} (no reload field)", overlay.getClass().getName());
                return;
            }
            Object reload = field.get(overlay);
            if (reload instanceof ReloadInstance reloadInstance) {
                context.logger().info("launchGame: overlay={} reloadDone={} progress={} fadeOutStarted={}",
                        overlay.getClass().getName(),
                        reloadInstance.isDone(),
                        reloadInstance.getActualProgress(),
                        overlay instanceof LoadingOverlay loadingOverlay && hasFadeOutStarted(loadingOverlay));
            }
        } catch (ReflectiveOperationException exception) {
            context.logger().info("launchGame: could not read reload state: {}", exception.toString());
        }
    }

    private static boolean isReloadDone(Overlay overlay) {
        try {
            Field field = resolveReloadField(overlay);
            if (field == null) {
                return false;
            }
            Object reload = field.get(overlay);
            return reload instanceof ReloadInstance reloadInstance && reloadInstance.isDone();
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean hasFadeOutStarted(LoadingOverlay overlay) {
        try {
            Field field = resolveFadeOutStartField();
            if (field == null) {
                return false;
            }
            return field.getLong(overlay) > -1L;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    /**
     * Last-resort: if {@link LoadingOverlay#tick()} could not start fade-out (for example
     * fade-in gate), invoke {@code onFinish} once so client init still completes.
     */
    @SuppressWarnings("unchecked")
    private static boolean ensureOnFinishInvoked(LoadingOverlay overlay) {
        if (hasFadeOutStarted(overlay)) {
            return true;
        }
        try {
            Field onFinishField = resolveOnFinishField();
            Field fadeOutField = resolveFadeOutStartField();
            if (onFinishField == null || fadeOutField == null) {
                return false;
            }
            Object onFinish = onFinishField.get(overlay);
            if (!(onFinish instanceof Consumer<?> consumer)) {
                return false;
            }
            Field reloadField = resolveReloadField(overlay);
            if (reloadField != null) {
                Object reload = reloadField.get(overlay);
                if (reload instanceof ReloadInstance reloadInstance) {
                    reloadInstance.checkExceptions();
                }
            }
            ((Consumer<Optional<Throwable>>) consumer).accept(Optional.empty());
            fadeOutField.setLong(overlay, System.currentTimeMillis());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Field resolveReloadField(Overlay overlay) throws ReflectiveOperationException {
        if (!loadingOverlayReloadFieldResolved) {
            loadingOverlayReloadFieldResolved = true;
            try {
                Field field = LoadingOverlay.class.getDeclaredField("reload");
                field.setAccessible(true);
                loadingOverlayReloadField = field;
            } catch (NoSuchFieldException ignored) {
                loadingOverlayReloadField = null;
            }
        }
        if (loadingOverlayReloadField != null) {
            return loadingOverlayReloadField;
        }
        Class<?> type = overlay.getClass();
        while (type != null && type != Object.class) {
            try {
                Field field = type.getDeclaredField("reload");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static Field resolveFadeOutStartField() {
        if (!loadingOverlayFadeOutStartFieldResolved) {
            loadingOverlayFadeOutStartFieldResolved = true;
            try {
                Field field = LoadingOverlay.class.getDeclaredField("fadeOutStart");
                field.setAccessible(true);
                loadingOverlayFadeOutStartField = field;
            } catch (NoSuchFieldException ignored) {
                loadingOverlayFadeOutStartField = null;
            }
        }
        return loadingOverlayFadeOutStartField;
    }

    private static Field resolveOnFinishField() {
        if (!loadingOverlayOnFinishFieldResolved) {
            loadingOverlayOnFinishFieldResolved = true;
            try {
                Field field = LoadingOverlay.class.getDeclaredField("onFinish");
                field.setAccessible(true);
                loadingOverlayOnFinishField = field;
            } catch (NoSuchFieldException ignored) {
                loadingOverlayOnFinishField = null;
            }
        }
        return loadingOverlayOnFinishField;
    }
}
