package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * First-enter ownership: an unowned TARDIS is claimed by a player who does not already own one.
 */
public final class TardisOwnershipLogic {
    private TardisOwnershipLogic() {
    }

    /**
     * Claims {@code tardisId} for {@code playerUuid} when the TARDIS is unowned and the player owns none.
     *
     * @return {@code true} if ownership was assigned
     */
    public static boolean tryClaimOnEnter(@Nullable UUID tardisId, @Nullable UUID playerUuid) {
        if (tardisId == null || playerUuid == null) {
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || model.ownerUuid != null) {
            return false;
        }
        if (TardisDataLoader.findOwnedBy(playerUuid).isPresent()) {
            return false;
        }
        model.setOwner(playerUuid);
        return true;
    }

    public static boolean isOwner(@Nullable TardisDataModel model, @Nullable UUID playerUuid) {
        return model != null && playerUuid != null && playerUuid.equals(model.ownerUuid);
    }
}
