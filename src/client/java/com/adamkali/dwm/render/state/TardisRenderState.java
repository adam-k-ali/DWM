package com.adamkali.dwm.render.state;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.MathHelper;

public class TardisRenderState extends EntityRenderState {
    private float doorSwingProgress;
    /** Vertical time-rotor bob offset in model units (0 when landed). */
    private float rotorBobOffset;

    public TardisRenderState() {
        this.doorSwingProgress = 0.0f;
        this.rotorBobOffset = 0.0f;
    }

    public void setDoorSwingProgress(float doorSwingProgress) {
        this.doorSwingProgress = MathHelper.clamp(doorSwingProgress, 0.0f, 1.0f);
    }

    public float getDoorSwingProgress() {
        return this.doorSwingProgress;
    }

    public void setRotorBobOffset(float rotorBobOffset) {
        this.rotorBobOffset = rotorBobOffset;
    }

    public float getRotorBobOffset() {
        return this.rotorBobOffset;
    }
}
