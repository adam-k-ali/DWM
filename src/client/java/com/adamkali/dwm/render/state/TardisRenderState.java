package com.adamkali.dwm.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

public class TardisRenderState extends EntityRenderState {
    private float doorSwingProgress;
    /** Vertical time-rotor bob offset in model units (0 when landed). */
    private float rotorBobOffset;
    /** Console stabilisers toggle (default on). Used by {@code StabilisersModel}. */
    private boolean stabilisersEnabled = true;
    /** 0–1 needle pose for the currently submitted reader / refueler. */
    private float needle;
    private boolean cloaked;

    public TardisRenderState() {
        this.doorSwingProgress = 0.0f;
        this.rotorBobOffset = 0.0f;
        this.stabilisersEnabled = true;
    }

    public void setDoorSwingProgress(float doorSwingProgress) {
        this.doorSwingProgress = Mth.clamp(doorSwingProgress, 0.0f, 1.0f);
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

    public void setStabilisersEnabled(boolean stabilisersEnabled) {
        this.stabilisersEnabled = stabilisersEnabled;
    }

    public boolean isStabilisersEnabled() {
        return this.stabilisersEnabled;
    }

    public void setNeedle(float needle) {
        this.needle = Mth.clamp(needle, 0.0f, 1.0f);
    }

    public float getNeedle() {
        return this.needle;
    }

    public void setCloaked(boolean cloaked) {
        this.cloaked = cloaked;
    }

    public boolean isCloaked() {
        return this.cloaked;
    }

}
