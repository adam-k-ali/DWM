package com.adamkali.dwm.mixin;

import com.adamkali.dwm.analytics.AnalyticsManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class WorldSaveMixin {

    @Inject(at = @At("RETURN"), method = "save")
    private void saveWorld(CallbackInfo info) {
        AnalyticsManager.deliver();
    }
}
