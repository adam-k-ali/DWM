package com.adamkali.dwm.mixin;

import com.adamkali.dwm.config.DWMConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void newPlayerEvent(CallbackInfo info) {
        if (DWMConfig.getBoolean(DWMConfig.IS_FIRST_START)) {
            DWMConfig.setBoolean(DWMConfig.IS_FIRST_START, false);
            DWMConfig.save();
        }
    }
}
