package com.adamkali.dwm.render.state;

import java.util.UUID;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * Extracted interior-door BER state for the MC 26.2 submit pipeline.
 */
public class TardisInteriorDoorBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float doorSwing;
    public float partialTicks;
    public boolean shouldRenderSoto;
    public UUID tardisId;
}
