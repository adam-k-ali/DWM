package com.adamkali.opendwm.actions;

import com.adamkali.opendwm.sound.DWMSounds;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Objects;

public class SonicActions {
    private static SonicActions INSTANCE;

    private SonicActions() {
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }

        return INSTANCE;
    }

    public void tryPerformAction(UseOnContext context) {
        context.getLevel().playLocalSound(context.getClickedPos(), DWMSounds.SONIC_THIRD_DOCTOR.get(), Objects.requireNonNull(context.getPlayer()).getSoundSource(), 1.0F, 1.0F, false);
        System.out.println("SonicActions.tryPerformAction");
    }


}
