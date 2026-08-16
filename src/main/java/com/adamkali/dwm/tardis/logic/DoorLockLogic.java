package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

/**
 * Door lock toggle. Opening is refused while locked; closing is always allowed.
 */
public final class DoorLockLogic {
    private DoorLockLogic() {
    }

    public static boolean isLocked(@Nullable TardisDataModel model) {
        return model != null && model.doorsLocked;
    }

    /**
     * Flips {@link TardisDataModel#doorsLocked}.
     *
     * @return the new locked state, or {@code false} when {@code model} is null
     */
    public static boolean toggle(@Nullable TardisDataModel model) {
        if (model == null) {
            return false;
        }
        model.doorsLocked = !model.doorsLocked;
        model.setChanged();
        return model.doorsLocked;
    }

    /** True when an attempt to open should be refused. */
    public static boolean blocksOpen(@Nullable TardisDataModel model, boolean currentlyOpen) {
        return isLocked(model) && !currentlyOpen;
    }
}
