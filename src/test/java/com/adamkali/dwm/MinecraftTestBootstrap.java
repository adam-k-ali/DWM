package com.adamkali.dwm;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

/**
 * Ensures Minecraft registries are bootstrapped for unit tests that touch blocks/items.
 */
public final class MinecraftTestBootstrap {
    private static boolean bootstrapped;

    private MinecraftTestBootstrap() {
    }

    public static synchronized void ensure() {
        if (bootstrapped) {
            return;
        }
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        bootstrapped = true;
    }
}
