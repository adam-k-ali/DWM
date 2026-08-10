package com.adamkali.dwm.render.state;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import java.util.UUID;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Extracted exterior TARDIS BER state for the MC 26.2 submit pipeline.
 */
public class TardisBlockEntityRenderState extends BlockEntityRenderState {
    public TardisChameleonVariant variant = TardisChameleonVariant.TT_CAPSULE;
    public float doorSwing;
    public float rotationDegrees;
    public float partialTicks;
    public boolean shouldRenderBoti;
    public UUID tardisId;
}
