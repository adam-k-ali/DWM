package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisExteriorLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Pure fast-return history helpers against a {@link TardisDataModel}.
 * History is LIFO: index 0 is the most recently departed exterior.
 */
public final class FastReturnLogic {
    public static final int MAX_HISTORY = 16;

    private FastReturnLogic() {
    }

    /** Ensures {@link TardisDataModel#locationHistory} is non-null after Gson load of older saves. */
    public static List<TardisExteriorLocation> history(@Nullable TardisDataModel model) {
        if (model == null) {
            return List.of();
        }
        return model.getLocationHistory();
    }

    /**
     * Pushes the model's current exterior onto the history head (LIFO).
     * Skips when exterior is missing or identical to the current head. Cap {@link #MAX_HISTORY}.
     *
     * @return true when a new entry was inserted
     */
    public static boolean pushDeparted(@Nullable TardisDataModel model) {
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null
                || model.exteriorDimension.isBlank()) {
            return false;
        }
        TardisExteriorLocation snapshot = TardisExteriorLocation.fromModel(model);
        List<TardisExteriorLocation> history = model.getLocationHistory();
        if (!history.isEmpty() && snapshot.equals(history.getFirst())) {
            return false;
        }
        history.addFirst(snapshot);
        while (history.size() > MAX_HISTORY) {
            history.removeLast();
        }
        model.setChanged();
        return true;
    }

    /**
     * Cycles the fast-return cursor: first click arms {@link DestinationMode#FAST_RETURN} at index 0;
     * further clicks walk further back and wrap.
     *
     * @return the newly selected location, or empty when history is empty
     */
    public static Optional<TardisExteriorLocation> cycle(@Nullable TardisDataModel model) {
        if (model == null) {
            return Optional.empty();
        }
        List<TardisExteriorLocation> history = model.getLocationHistory();
        if (history.isEmpty()) {
            return Optional.empty();
        }
        if (model.getDestinationMode() != DestinationMode.FAST_RETURN) {
            model.selectedWaypointId = null;
            model.selectedPlayerUuid = null;
            model.selectedFastReturnIndex = 0;
            model.setDestinationMode(DestinationMode.FAST_RETURN);
        } else {
            int next = model.selectedFastReturnIndex + 1;
            if (next >= history.size()) {
                next = 0;
            }
            model.selectedFastReturnIndex = next;
            model.setChanged();
        }
        return selected(model);
    }

    /** Current fast-return selection when mode is armed and index is in range. */
    public static Optional<TardisExteriorLocation> selected(@Nullable TardisDataModel model) {
        if (!hasSelection(model)) {
            return Optional.empty();
        }
        return Optional.of(model.getLocationHistory().get(model.selectedFastReturnIndex));
    }

    public static boolean hasSelection(@Nullable TardisDataModel model) {
        if (model == null || model.getDestinationMode() != DestinationMode.FAST_RETURN) {
            return false;
        }
        List<TardisExteriorLocation> history = model.getLocationHistory();
        int index = model.selectedFastReturnIndex;
        return !history.isEmpty() && index >= 0 && index < history.size();
    }

    /** After a successful landing, keep FAST_RETURN armed at the newest history head. */
    public static void resetIndexAfterLanding(@Nullable TardisDataModel model) {
        if (model == null) {
            return;
        }
        if (model.getDestinationMode() == DestinationMode.FAST_RETURN) {
            model.selectedFastReturnIndex = 0;
            model.setChanged();
        }
    }
}
