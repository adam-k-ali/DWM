package com.adamkali.sightline.platform;

import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

/**
 * Loader-specific hooks required by the shared Sightline bootstrap.
 */
public interface SightlinePlatform {
    /**
     * Registers a callback invoked once at the end of every client tick.
     */
    void registerEndClientTick(Consumer<Minecraft> tickHandler);
}
