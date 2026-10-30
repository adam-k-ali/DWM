package com.adamkali.dwm.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Animation inputs for the TARDIS globe mesh: the ambient clock rides in {@link #ageInTicks},
 * plus the interaction spin (degrees) and its 0-1 energy envelope. All zero is the rest pose.
 */
public class TardisGlobeRenderState extends EntityRenderState {
    public float spinDegrees;
    public float spinEnergy;
}
