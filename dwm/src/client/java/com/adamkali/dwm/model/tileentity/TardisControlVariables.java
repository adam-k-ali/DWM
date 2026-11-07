package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.anim.AnimationVariables;
import com.adamkali.dwm.render.state.TardisRenderState;

/**
 * Animation inputs shared by the console controls (levers, switches, readers, time rotor). Booleans are
 * exposed as {@code 0} / {@code 1} so JSON expressions can scale a pose by them.
 *
 * <ul>
 *   <li>{@code doors_locked}, {@code cloaked}, {@code stabilisers}, {@code lock_x}, {@code lock_y},
 *       {@code lock_z}: control state, 0 or 1</li>
 *   <li>{@code needle}: reader fill, 0-1</li>
 *   <li>{@code rotor_bob}: time-rotor vertical offset (model units, never above rest)</li>
 *   <li>{@code rotor_spin}: time-middle spin (radians; multiply by {@code deg} for a degree channel)</li>
 * </ul>
 */
public final class TardisControlVariables {
    public static final AnimationVariables<TardisRenderState> VARIABLES = AnimationVariables.<TardisRenderState>builder()
            .add("doors_locked", state -> state.areDoorsLocked() ? 1.0 : 0.0)
            .add("cloaked", state -> state.isCloaked() ? 1.0 : 0.0)
            .add("stabilisers", state -> state.isStabilisersEnabled() ? 1.0 : 0.0)
            .add("lock_x", state -> state.isLockX() ? 1.0 : 0.0)
            .add("lock_y", state -> state.isLockY() ? 1.0 : 0.0)
            .add("lock_z", state -> state.isLockZ() ? 1.0 : 0.0)
            .add("needle", TardisRenderState::getNeedle)
            .add("rotor_bob", TardisRenderState::getRotorBobOffset)
            .add("rotor_spin", TardisRenderState::getRotorSpinRadians)
            .build();

    private TardisControlVariables() {
    }
}
