package com.adamkali.opendwm.actions;

import com.adamkali.opendwm.sound.DWMSounds;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;

import java.util.HashMap;
import java.util.Objects;

public class SonicActions {
    private static SonicActions INSTANCE;

    private HashMap<Block, BlockModificationAction> actions = new HashMap<>();


    private SonicActions() {
        this.actions.put(Blocks.IRON_DOOR, (level, blockPos, blockState, player) -> {
            boolean isOpen = blockState.getValue(DoorBlock.OPEN);
            ((DoorBlock) blockState.getBlock()).setOpen(player, level, blockState, blockPos, !isOpen);
            level.updateNeighborsAt(blockPos, blockState.getBlock());
        });
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }

        return INSTANCE;
    }

    public void tryPerformAction(UseOnContext context) {
        context.getLevel().playLocalSound(context.getClickedPos(), DWMSounds.SONIC_THIRD_DOCTOR.get(), Objects.requireNonNull(context.getPlayer()).getSoundSource(), 1.0F, 1.0F, false);
        Block blockClicked = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        if (this.actions.containsKey(blockClicked)) {
            this.actions.get(blockClicked).perform(context.getLevel(), context.getClickedPos(), context.getLevel().getBlockState(context.getClickedPos()), context.getPlayer());
        }
    }


}
