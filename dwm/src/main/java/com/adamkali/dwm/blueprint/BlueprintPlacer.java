package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.Blueprint;
import com.adamkali.dwm.blueprint.model.BlueprintBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places a rasterized blueprint into a {@link ServerLevel} at the given origin.
 */
public final class BlueprintPlacer {
    private BlueprintPlacer() {
    }

    /**
     * Rasterizes, resolves all blocks (failing fast on unknown ids), then places them.
     *
     * @return number of blocks placed
     * @throws UnknownBlueprintBlockException if any block id cannot be resolved
     * @throws BlueprintTooLargeException if rasterization exceeds the voxel cap
     */
    public static int place(ServerLevel world, BlockPos origin, Blueprint blueprint) {
        Map<BlockPos, BlueprintBlock> relative = BlueprintRasterizer.rasterize(blueprint);
        Map<BlockPos, BlockState> resolved = new LinkedHashMap<>(relative.size());
        for (Map.Entry<BlockPos, BlueprintBlock> entry : relative.entrySet()) {
            Optional<BlockState> state = BlueprintBlockResolver.resolve(entry.getValue());
            if (state.isEmpty()) {
                throw new UnknownBlueprintBlockException(entry.getValue().id());
            }
            resolved.put(entry.getKey(), state.get());
        }
        for (Map.Entry<BlockPos, BlockState> entry : resolved.entrySet()) {
            BlockPos worldPos = origin.offset(entry.getKey());
            world.setBlock(worldPos, entry.getValue(), Block.UPDATE_ALL);
        }
        return resolved.size();
    }
}
