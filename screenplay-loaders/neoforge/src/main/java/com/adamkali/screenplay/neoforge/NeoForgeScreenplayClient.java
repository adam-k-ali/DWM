package com.adamkali.screenplay.neoforge;

import com.adamkali.screenplay.ScreenplayBootstrap;
import com.adamkali.screenplay.platform.ScreenplayPlatform;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.function.Consumer;

@Mod(value = ScreenplayBootstrap.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ScreenplayBootstrap.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeScreenplayClient {
    private static Consumer<Minecraft> tickHandler;

    public NeoForgeScreenplayClient() {
        ScreenplayBootstrap.start(new NeoForgePlatform());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (tickHandler != null) {
            tickHandler.accept(Minecraft.getInstance());
        }
    }

    private static final class NeoForgePlatform implements ScreenplayPlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> handler) {
            tickHandler = handler;
        }
    }
}
