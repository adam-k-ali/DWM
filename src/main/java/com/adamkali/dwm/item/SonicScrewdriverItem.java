package com.adamkali.dwm.item;

import com.adamkali.dwm.actions.SonicActions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class SonicScrewdriverItem extends Item {
    public SonicScrewdriverItem(Properties itemProperties) {
        super(itemProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        SonicActions.getInstance().tryPerformAction(context);
        return super.useOn(context);
    }
}
