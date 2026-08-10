package com.adamkali.dwm.render;

/**
 * Debug wireframes for First Doctor console hit regions (F3+B).
 * <p>
 * Minecraft 26.2 replaced the immediate-mode WorldRender/ShapeRenderer path with gizmos.
 * Re-implement against {@code LevelRenderEvents.BEFORE_GIZMOS} in a follow-up; keep registration
 * so client init stays stable during the port.
 */
public final class ConsoleHitboxDebugRenderer {
    private ConsoleHitboxDebugRenderer() {
    }

    public static void initialize() {
        // Intentionally empty until gizmo-based debug drawing is ported.
    }
}
