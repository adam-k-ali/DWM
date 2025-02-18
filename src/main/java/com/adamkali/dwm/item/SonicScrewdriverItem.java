package com.adamkali.dwm.item;

import com.adamkali.dwm.actions.SonicActions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class SonicScrewdriverItem extends Item {
    public SonicScrewdriverItem(Item.Settings itemProperties) {
        super(itemProperties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        SonicActions.getInstance().tryPerformAction(context);
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        SonicActions.getInstance().interactWithEntity(entity, user);
        return super.useOnEntity(stack, user, entity, hand);
    }
}
