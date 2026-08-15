package com.adamkali.dwm.tardis.boti;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Packs relative BOTI footprint coords (0..31 per axis) into a short for network transfer.
 * Five bits per axis supports the 11×7×17 First Doctor console-room footprint.
 */
public final class BotiRelativePosCodec {
    private BotiRelativePosCodec() {
    }

    public static short pack(BlockPos relative) {
        return pack(relative.getX(), relative.getY(), relative.getZ());
    }

    public static short pack(int x, int y, int z) {
        return (short) ((x & 0x1F) | ((y & 0x1F) << 5) | ((z & 0x1F) << 10));
    }

    public static BlockPos unpack(short packed) {
        int x = packed & 0x1F;
        int y = (packed >> 5) & 0x1F;
        int z = (packed >> 10) & 0x1F;
        return new BlockPos(x, y, z);
    }

    public static int stateId(BlockState state) {
        return Block.getId(state);
    }

    public static BlockState stateFromId(int id) {
        return Block.stateById(id);
    }
}
