package com.adamkali.dwm.tardis.portal;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * World-absolute block/BE sample for one chunk column in a portal stream.
 */
public record PortalStreamSample(
        int chunkX,
        int chunkZ,
        Map<BlockPos, BlockState> blocks,
        Map<BlockPos, CompoundTag> blockEntities
) {
}
