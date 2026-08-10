package com.adamkali.dwm.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * Extracted First Doctor console BER state for the MC 26.2 submit pipeline.
 */
public class FirstDoctorConsoleBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float rotorBobOffset;
}
