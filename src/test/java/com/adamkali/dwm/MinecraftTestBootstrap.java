package com.adamkali.dwm;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

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
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        bootstrapped = true;
    }
}
