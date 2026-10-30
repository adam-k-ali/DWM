package com.adamkali.dwm.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Passive block entity for interior decor BER meshes (globe, scanners). No NBT or tick; the globe's
 * spin is transient and started by a block event.
 */
public class TardisDecorBlockEntity extends BlockEntity {
    /** Block event id sent by {@code TardisGlobeBlock} on use. */
    public static final int EVENT_SPIN = 1;

    private final TardisGlobeSpin spin = new TardisGlobeSpin();

    public TardisDecorBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_DECOR_BLOCK_ENTITY, pos, state);
    }

    public TardisGlobeSpin spin() {
        return spin;
    }

    @Override
    public boolean triggerEvent(int b0, int b1) {
        if (b0 == EVENT_SPIN) {
            if (level != null) {
                spin.start(level.getGameTime());
            }
            return true;
        }
        return super.triggerEvent(b0, b1);
    }
}
