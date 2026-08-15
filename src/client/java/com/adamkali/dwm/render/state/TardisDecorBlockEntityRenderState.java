package com.adamkali.dwm.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;

/**
 * Extracted facing and block identity for static interior-decor BER submit.
 */
public class TardisDecorBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public Block block = Blocks.AIR;
}
