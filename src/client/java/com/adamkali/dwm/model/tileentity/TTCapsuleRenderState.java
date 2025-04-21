package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.MathHelper;

public class TTCapsuleRenderState extends EntityRenderState {
    private float doorSwingProgress;
    public TTCapsuleRenderState() {
        this.doorSwingProgress = 0.0f;
    }

    public void setDoorSwingProgress(float doorSwingProgress) {
        this.doorSwingProgress = MathHelper.clamp(doorSwingProgress, 0.0f, 1.0f);
    }

    public float getDoorSwingProgress() {
        return this.doorSwingProgress;
    }
}
