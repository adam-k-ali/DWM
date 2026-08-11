package com.adamkali.dwm.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;

/**
 * Extracted First Doctor console BER state for the MC 26.2 submit pipeline.
 */
public class FirstDoctorConsoleBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float rotorBobOffset;
    /** Synced chameleon variant for the Panel6 hologram shell. */
    public TardisChameleonVariant variant = TardisChameleonVariant.TT_CAPSULE;
}
