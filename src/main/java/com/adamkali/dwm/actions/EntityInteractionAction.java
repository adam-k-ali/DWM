package com.adamkali.dwm.actions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface EntityInteractionAction {
    void perform(LivingEntity entity, Player player, ServerLevel level, InteractionHand hand);
}
