package com.adamkali.screenplay.platform;

import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

/**
 * Loader-specific hooks required by the shared Screenplay bootstrap.
 */
public interface ScreenplayPlatform {
    /**
     * Registers a callback invoked once at the end of every client tick.
     */
    void registerEndClientTick(Consumer<Minecraft> tickHandler);
}
