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

        // LoadingOverlay only advances fade/finish from Overlay.tick(), which is gated on
        // Minecraft client ticks. Under Forge/xvfb those ticks can stall while frames still
        // render — drive the overlay from Screenplay so the title screen can appear.
        Overlay overlay = client.gui.overlay();
        if (overlay != null) {
            overlay.tick();
            maybeLogReloadProgress(context, overlay);
            if (overlay instanceof LoadingOverlay && isReloadDone(overlay)
                    && !(context.screen() instanceof TitleScreen)) {
                // onFinish should have swapped to the title screen; force it if fade stalled.
                client.gui.setOverlay(null);
                client.gui.setScreen(new TitleScreen());
            }
        }

        if (context.screen() instanceof AccessibilityOnboardingScreen) {
            client.gui.setScreen(new TitleScreen());
            return false;
        }
        return context.screen() instanceof TitleScreen;
    }

    private static void maybeLogReloadProgress(ScenarioPrimitiveContext context, Overlay overlay) {
        long now = System.nanoTime();
        if (lastReloadLogNanos != 0L && now - lastReloadLogNanos < 5_000_000_000L) {
            return;
        }
        lastReloadLogNanos = now;
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
        // ForgeLoadingOverlay shadows reload on the subclass.
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
