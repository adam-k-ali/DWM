package com.adamkali.sightline.fabric;

import com.adamkali.sightline.SightlineBootstrap;
import com.adamkali.sightline.platform.SightlinePlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public final class FabricSightlineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SightlineBootstrap.start(new FabricPlatform());
    }

    private static final class FabricPlatform implements SightlinePlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> tickHandler) {
            ClientTickEvents.END_CLIENT_TICK.register(tickHandler::accept);
        }
    }
}
