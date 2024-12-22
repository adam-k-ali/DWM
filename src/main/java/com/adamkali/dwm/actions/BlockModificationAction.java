package com.adamkali.dwm.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockModificationAction {
    void perform(Level level, BlockPos blockPos, BlockState blockState, Player player);
}
