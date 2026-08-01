package com.adamkali.dwm.tardis.boti;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Packs relative BOTI footprint coords (0..10) into a short for network transfer.
 */
public final class BotiRelativePosCodec {
    private BotiRelativePosCodec() {
    }

    public static short pack(BlockPos relative) {
        return pack(relative.getX(), relative.getY(), relative.getZ());
    }

    public static short pack(int x, int y, int z) {
        return (short) ((x & 0xF) | ((y & 0xF) << 4) | ((z & 0xF) << 8));
    }

    public static BlockPos unpack(short packed) {
        int x = packed & 0xF;
        int y = (packed >> 4) & 0xF;
        int z = (packed >> 8) & 0xF;
        return new BlockPos(x, y, z);
    }

    public static int stateId(BlockState state) {
        return Block.getRawIdFromState(state);
    }

    public static BlockState stateFromId(int id) {
        return Block.getStateFromRawId(id);
    }
}
