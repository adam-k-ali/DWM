package com.adamkali.dwm.item;

import com.adamkali.dwm.actions.SonicActions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class SonicScrewdriverItem extends Item {
    public SonicScrewdriverItem(Item.Properties itemProperties) {
        super(itemProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        SonicActions.getInstance().interactWithBlock(context);
        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        SonicActions.getInstance().interactWithEntity(stack, entity, user, hand);
        return super.interactLivingEntity(stack, user, entity, hand);
    }
}
