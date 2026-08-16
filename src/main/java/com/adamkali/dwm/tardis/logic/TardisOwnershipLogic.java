package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * First-enter ownership plus ops overwrite: one TARDIS per player.
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

    /**
     * Overwrites {@code tardisId} ownership to {@code playerUuid} unless the player already owns a
     * different TARDIS.
     */
    public static ForceClaimResult tryForceClaim(@Nullable UUID tardisId, @Nullable UUID playerUuid) {
        if (tardisId == null || playerUuid == null) {
            return ForceClaimResult.INVALID;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return ForceClaimResult.UNKNOWN;
        }
        if (playerUuid.equals(model.ownerUuid)) {
            return ForceClaimResult.ALREADY_OWNER;
        }
        Optional<TardisDataModel> owned = TardisDataLoader.findOwnedBy(playerUuid);
        if (owned.isPresent() && !tardisId.equals(owned.get().uuid)) {
            return ForceClaimResult.PLAYER_OWNS_ANOTHER;
        }
        model.setOwner(playerUuid);
        return ForceClaimResult.CLAIMED;
    }

    public static boolean isOwner(@Nullable TardisDataModel model, @Nullable UUID playerUuid) {
        return model != null && playerUuid != null && playerUuid.equals(model.ownerUuid);
    }

    public enum ForceClaimResult {
        CLAIMED,
        ALREADY_OWNER,
        PLAYER_OWNS_ANOTHER,
        UNKNOWN,
        INVALID
    }
}
