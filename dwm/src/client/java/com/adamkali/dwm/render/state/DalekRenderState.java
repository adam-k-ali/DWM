package com.adamkali.dwm.render.state;

import com.adamkali.dwm.entity.DalekVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class DalekRenderState extends LivingEntityRenderState {
    public DalekVariant variant = DalekVariant.CLASSIC_1963;
    public boolean flying;
    public float leanPitch;
    public float leanRoll;
}
