package com.adamkali.screenplay.mixin;

import com.adamkali.screenplay.HeldPhysicalKeys;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets Screenplay {@code pressKey: left}/{@code right} satisfy InputConstants polls such as the
 * sonic field-mode HUD carousel.
 */
@Mixin(InputConstants.class)
public class InputConstantsIsKeyDownMixin {
    @Inject(method = "isKeyDown", at = @At("RETURN"), cancellable = true)
    private static void screenplay$orHeldPhysicalKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && HeldPhysicalKeys.isHeld(key)) {
            cir.setReturnValue(true);
        }
    }
}
