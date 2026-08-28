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
        Map<BlockPos, CompoundTag> blockEntities,
        PortalLightData lightData
) {
    public PortalStreamSample(
            int chunkX,
            int chunkZ,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntities
    ) {
        this(chunkX, chunkZ, blocks, blockEntities, PortalLightData.EMPTY);
    }

    public PortalStreamSample {
        blocks = blocks == null ? Map.of() : Map.copyOf(blocks);
        blockEntities = blockEntities == null ? Map.of() : Map.copyOf(blockEntities);
        lightData = lightData == null ? PortalLightData.EMPTY : lightData;
    }
}
