package com.adamkali.sightline.forge;

import com.adamkali.sightline.SightlineBootstrap;
import com.adamkali.sightline.platform.SightlinePlatform;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod(SightlineBootstrap.MOD_ID)
public final class ForgeSightlineClient {
    public ForgeSightlineClient() {
        SightlineBootstrap.start(new ForgePlatform());
    }

    private static final class ForgePlatform implements SightlinePlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> tickHandler) {
            TickEvent.ClientTickEvent.Post.BUS.addListener(event -> tickHandler.accept(Minecraft.getInstance()));
        }
    }
}
