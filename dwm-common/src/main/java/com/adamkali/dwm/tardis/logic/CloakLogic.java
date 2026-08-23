package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

/**
 * Perception-filter cloak toggle. When engaged, the exterior BER hides the shell.
 */
public final class CloakLogic {
    private CloakLogic() {
    }

    public static boolean isCloaked(@Nullable TardisDataModel model) {
        return model != null && model.cloaked;
    }

    /**
     * Flips {@link TardisDataModel#cloaked}.
     *
     * @return the new cloaked state, or {@code false} when {@code model} is null
     */
    public static boolean toggle(@Nullable TardisDataModel model) {
        if (model == null) {
            return false;
        }
        model.cloaked = !model.cloaked;
        model.setChanged();
        return model.cloaked;
    }
}
