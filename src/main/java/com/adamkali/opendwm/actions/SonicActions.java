package com.adamkali.opendwm.actions;

import net.minecraft.world.item.context.UseOnContext;

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
        System.out.println("SonicActions.tryPerformAction");
    }


}
