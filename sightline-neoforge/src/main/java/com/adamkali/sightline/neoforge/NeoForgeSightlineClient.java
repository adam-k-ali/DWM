package com.adamkali.sightline.neoforge;

import com.adamkali.sightline.SightlineBootstrap;
import com.adamkali.sightline.platform.SightlinePlatform;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.function.Consumer;

@Mod(value = SightlineBootstrap.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SightlineBootstrap.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeSightlineClient {
    private static Consumer<Minecraft> tickHandler;

    public NeoForgeSightlineClient() {
        SightlineBootstrap.start(new NeoForgePlatform());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (tickHandler != null) {
            tickHandler.accept(Minecraft.getInstance());
        }
    }

    private static final class NeoForgePlatform implements SightlinePlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> handler) {
            tickHandler = handler;
        }
    }
}
