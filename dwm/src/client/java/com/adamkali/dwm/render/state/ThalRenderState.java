package com.adamkali.dwm.render.state;

import com.adamkali.dwm.entity.ThalVariant;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;

public class ThalRenderState extends ArmedEntityRenderState {
    public ThalVariant variant = ThalVariant.THAL_1;
    public boolean armed;
}
