package com.adamkali.screenplay.primitive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.ReloadInstance;

import java.lang.reflect.Field;
import java.util.Map;

public final class LaunchGamePrimitive extends NoArgPrimitive {
    private static Field loadingOverlayReloadField;
    private static boolean loadingOverlayReloadFieldResolved;
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

        // Once resource reload reports done, force the title screen. Under Forge/xvfb,
        // LoadingOverlay.tick() (which normally calls onFinish) may never run because
        // Minecraft client ticks stall while frames still render.
        if (overlay instanceof LoadingOverlay && isReloadDone(overlay)) {
            client.gui.setOverlay(null);
            if (!(context.screen() instanceof TitleScreen)) {
                client.gui.setScreen(new TitleScreen());
            }
        }

        // Last-resort escape hatch: if reload progress is nearly complete but isDone never
        // flips (Forge ClientModLoader sync stall under xvfb), still enter the title screen.
        if (overlay instanceof LoadingOverlay) {
            float progress = reloadProgress(overlay);
            if (progress >= 0.99f) {
                client.gui.setOverlay(null);
                if (!(context.screen() instanceof TitleScreen)) {
                    client.gui.setScreen(new TitleScreen());
                }
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
                context.logger().info("launchGame: overlay={} reloadDone={} progress={}",
                        overlay.getClass().getName(),
                        reloadInstance.isDone(),
                        reloadInstance.getActualProgress());
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

    private static float reloadProgress(Overlay overlay) {
        try {
            Field field = resolveReloadField(overlay);
            if (field == null) {
                return 0f;
            }
            Object reload = field.get(overlay);
            if (reload instanceof ReloadInstance reloadInstance) {
                return reloadInstance.getActualProgress();
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through
        }
        return 0f;
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
}
