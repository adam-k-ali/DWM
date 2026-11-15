package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.TardisTransitionFade;
import com.adamkali.dwm.tardis.logic.TardisTransitionFadeLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Flags TARDIS dimension changes so the loading screen can be replaced by a smooth fade. */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerTardisMixin {
    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void dwm$flagTardisTransition(ClientboundRespawnPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        TardisTransitionFade.begin(TardisTransitionFadeLogic.isTardisTransition(
                minecraft.player.level().dimension(), packet.commonPlayerSpawnInfo().dimension()));
    }
}
