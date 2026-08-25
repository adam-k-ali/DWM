package com.adamkali.screenplay.neoforge;

import com.adamkali.screenplay.ScreenplayBootstrap;
import com.adamkali.screenplay.platform.ScreenplayPlatform;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.function.Consumer;

@Mod(value = ScreenplayBootstrap.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ScreenplayBootstrap.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeScreenplayClient {
    private static Consumer<Minecraft> tickHandler;

    public NeoForgeScreenplayClient() {
        ScreenplayBootstrap.start(new NeoForgePlatform());
    }

    @SubscribeEvent
    static void onRenderFrame(RenderFrameEvent.Post event) {
        // Render frames keep Screenplay advancing under headless/xvfb menu load.
        dispatch();
    }

    private static void dispatch() {
        if (tickHandler != null) {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                tickHandler.accept(client);
            }
        }
    }

    private static final class NeoForgePlatform implements ScreenplayPlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> handler) {
            tickHandler = handler;
        }
    }
}
