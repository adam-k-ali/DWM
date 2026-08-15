package com.adamkali.dwm.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Passive block entity for static interior decor BER meshes (globe, scanners). No NBT or tick.
 */
public class TardisDecorBlockEntity extends BlockEntity {
    public TardisDecorBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_DECOR_BLOCK_ENTITY, pos, state);
    }
}
