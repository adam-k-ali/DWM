package com.adamkali.dwm.actions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

@FunctionalInterface
public interface EntityInteractionAction {
    void perform(LivingEntity entity, PlayerEntity player, ServerWorld level, Hand hand);
}
