package com.adamkali.dwm.actions;


import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface BlockModificationAction {
    void perform(World level, BlockPos blockPos, BlockState blockState, PlayerEntity player);
}
