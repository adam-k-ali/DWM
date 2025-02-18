package com.adamkali.dwm.actions;

import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.block.*;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;

public class SonicActions {
    private static SonicActions INSTANCE;

    private final HashMap<Block, BlockModificationAction> actions = new HashMap<>();


    private SonicActions() {
        this.actions.put(Blocks.TNT, (level, blockPos, blockState, player) -> {
            level.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            TntBlock.primeTnt(level, blockPos);
        });
        this.actions.put(Blocks.IRON_DOOR, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(DoorBlock.OPEN);
            DoorBlock block = (DoorBlock) blockState.getBlock();
            boolean open = newState.get(DoorBlock.OPEN);
            level.setBlockState(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? block.getBlockSetType().doorOpen() : block.getBlockSetType().doorClose(), SoundCategory.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.emitGameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        });
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }

        return INSTANCE;
    }

    public void tryPerformAction(ItemUsageContext context) {
        context.getWorld().playSoundAtBlockCenter(context.getBlockPos(), DWMSounds.SONIC_THIRD_DOCTOR, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        Block blockClicked = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (this.actions.containsKey(blockClicked)) {
            this.actions.get(blockClicked).perform(context.getWorld(), context.getBlockPos(), context.getWorld().getBlockState(context.getBlockPos()), context.getPlayer());
        }
    }


}
