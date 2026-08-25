package com.adamkali.screenplay.fabric;

import com.adamkali.screenplay.ScreenplayBootstrap;
import com.adamkali.screenplay.platform.ScreenplayPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public final class FabricScreenplayClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenplayBootstrap.start(new FabricPlatform());
    }

    private static final class FabricPlatform implements ScreenplayPlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> tickHandler) {
            ClientTickEvents.END_CLIENT_TICK.register(tickHandler::accept);
        }
    }
}
