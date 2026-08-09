package com.adamkali.dwm.block.wood;

import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

public record WoodFamilyBlocks(
        Block planks,
        Block log,
        Block wood,
        Block strippedLog,
        Block strippedWood,
        Block leaves,
        Block sapling,
        Block pottedSapling,
        Block stairs,
        Block slab,
        Block fence,
        Block fenceGate,
        Block button,
        Block pressurePlate,
        Block sign,
        Block wallSign,
        Block hangingSign,
        Block wallHangingSign,
        @Nullable Block door,
        @Nullable Block trapdoor
) {
}
