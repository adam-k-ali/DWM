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
            // Render ticks fire every frame under xvfb; ClientTickEvent alone can stall during menu load.
            TickEvent.RenderTickEvent.Post.BUS.addListener(event -> {
                Minecraft client = Minecraft.getInstance();
                if (client != null) {
                    tickHandler.accept(client);
                }
            });
        }
    }
}
