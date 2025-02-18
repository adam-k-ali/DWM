package com.adamkali.dwm.actions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

@FunctionalInterface
public interface EntityInteractionAction {
    void perform(LivingEntity entity, PlayerEntity player, ServerWorld level);
}
