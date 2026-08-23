package com.adamkali.screenplay.forge;

import com.adamkali.screenplay.ScreenplayBootstrap;
import com.adamkali.screenplay.platform.ScreenplayPlatform;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod(ScreenplayBootstrap.MOD_ID)
public final class ForgeScreenplayClient {
    public ForgeScreenplayClient() {
        ScreenplayBootstrap.start(new ForgePlatform());
    }

    private static final class ForgePlatform implements ScreenplayPlatform {
        @Override
        public void registerEndClientTick(Consumer<Minecraft> tickHandler) {
            TickEvent.ClientTickEvent.Post.BUS.addListener(event -> tickHandler.accept(Minecraft.getInstance()));
        }
    }
}
