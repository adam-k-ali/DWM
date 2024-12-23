package com.adamkali.dwm.item;

import com.adamkali.dwm.actions.SonicActions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public class SonicScrewdriverItem extends Item {
    public SonicScrewdriverItem(Item.Settings itemProperties) {
        super(itemProperties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        SonicActions.getInstance().tryPerformAction(context);
        return super.useOnBlock(context);
    }
}
