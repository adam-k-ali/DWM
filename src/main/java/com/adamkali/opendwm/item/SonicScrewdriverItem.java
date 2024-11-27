package com.adamkali.opendwm.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class SonicScrewdriverItem extends Item {
    public SonicScrewdriverItem(Properties itemProperties) {
        super(itemProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        System.out.println("SonicScrewdriverItem.useOn");
        return super.useOn(p_41427_);
    }
}
