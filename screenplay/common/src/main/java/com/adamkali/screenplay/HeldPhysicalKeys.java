package com.adamkali.screenplay;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GLFW key codes Screenplay is holding for at least one client tick.
 * {@code InputConstants.isKeyDown} is mixed in on Fabric so HUD code that polls
 * GLFW (rather than {@code KeyMapping}) observes the hold.
 */
public final class HeldPhysicalKeys {
    private static final Set<Integer> HELD = ConcurrentHashMap.newKeySet();

    private HeldPhysicalKeys() {
    }

    public static void hold(int keyCode) {
        HELD.add(keyCode);
    }

    public static void release(int keyCode) {
        HELD.remove(keyCode);
    }

    public static boolean isHeld(int keyCode) {
        return HELD.contains(keyCode);
    }

    public static void clear() {
        HELD.clear();
    }
}
