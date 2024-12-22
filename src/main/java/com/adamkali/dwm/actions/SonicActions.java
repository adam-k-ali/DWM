package com.adamkali.dwm.actions;

import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;

import java.util.HashMap;
import java.util.Objects;

public class SonicActions {
    private static SonicActions INSTANCE;

    private HashMap<Block, BlockModificationAction> actions = new HashMap<>();


    private SonicActions() {
        this.actions.put(Blocks.TNT, (level, blockPos, blockState, player) -> {
            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            TntBlock.explode(level, blockPos);
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
