package com.adamkali.dwm.render.state;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.MathHelper;

public class TardisRenderState extends EntityRenderState {
    private float doorSwingProgress;
    public TardisRenderState() {
        this.doorSwingProgress = 0.0f;
    }

    public void setDoorSwingProgress(float doorSwingProgress) {
        this.doorSwingProgress = MathHelper.clamp(doorSwingProgress, 0.0f, 1.0f);
    }

    public float getDoorSwingProgress() {
        return this.doorSwingProgress;
    }
}
