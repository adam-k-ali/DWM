package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Determines whether a TARDIS key can be bound or used with a TARDIS.
 * Keys retain only the TARDIS identifier, so ownership changes do not invalidate them.
 */
public final class TardisKeyLogic {
    private TardisKeyLogic() {
    }

    public static UseResult useOnTardis(
            @Nullable UUID boundTardisId,
            @Nullable UUID playerUuid,
            @Nullable UUID clickedTardisId,
            @Nullable TardisDataModel model
    ) {
        if (clickedTardisId == null || model == null) {
            return UseResult.UNAVAILABLE;
        }

        if (boundTardisId == null) {
            if (!TardisOwnershipLogic.isOwner(model, playerUuid)) {
                return UseResult.NOT_OWNER;
            }
            return UseResult.BOUND;
        }

        return boundTardisId.equals(clickedTardisId)
                ? UseResult.TOGGLE_READY
                : UseResult.WRONG_TARDIS;
    }

    public enum UseResult {
        BOUND,
        TOGGLE_READY,
        NOT_OWNER,
        WRONG_TARDIS,
        UNAVAILABLE
    }
}
