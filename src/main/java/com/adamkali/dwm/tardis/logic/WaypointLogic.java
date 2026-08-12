package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure waypoint CRUD helpers against a {@link TardisDataModel}.
 * Waypoint coordinates are the linked TARDIS exterior location, not interior player coords.
 */
public final class WaypointLogic {
    public static final int MAX_WAYPOINTS = 16;

    private WaypointLogic() {
    }

    public static Optional<TardisWaypoint> find(@Nullable TardisDataModel model, @Nullable UUID waypointId) {
        if (model == null || waypointId == null) {
            return Optional.empty();
        }
        for (TardisWaypoint waypoint : model.getWaypoints()) {
            if (waypoint != null && waypointId.equals(waypoint.id)) {
                return Optional.of(waypoint);
            }
        }
        return Optional.empty();
    }

    /** Convenience accessor used by console/network code. */
    public static List<TardisWaypoint> waypoints(@Nullable TardisDataModel model) {
        if (model == null) {
            return List.of();
        }
        return model.getWaypoints();
    }

    public static boolean isNameUnique(@Nullable TardisDataModel model, @Nullable String name) {
        return isNameUnique(model, name, null);
    }

    /**
     * @param excludingId when non-null, that waypoint's name is ignored (for rename flows)
     */
    public static boolean isNameUnique(
            @Nullable TardisDataModel model,
            @Nullable String name,
            @Nullable UUID excludingId
    ) {
        if (model == null || name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim();
        for (TardisWaypoint waypoint : model.getWaypoints()) {
            if (waypoint == null || waypoint.name == null) {
                continue;
            }
            if (excludingId != null && excludingId.equals(waypoint.id)) {
                continue;
            }
            if (normalized.equalsIgnoreCase(waypoint.name.trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a waypoint at the model's current exterior location.
     *
     * @param requestedName optional display name; blank → generated unique name
     * @return the created waypoint, or empty when exterior missing, at cap, or name taken
     */
    public static Optional<TardisWaypoint> add(
            @Nullable TardisDataModel model,
            @Nullable String requestedName
    ) {
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null
                || model.exteriorDimension.isBlank()) {
            return Optional.empty();
        }
        List<TardisWaypoint> waypoints = model.getWaypoints();
        if (waypoints.size() >= MAX_WAYPOINTS) {
            return Optional.empty();
        }

        String name;
        if (requestedName == null || requestedName.isBlank()) {
            name = nextGeneratedName(model);
        } else {
            name = requestedName.trim();
            if (!isNameUnique(model, name)) {
                return Optional.empty();
            }
        }

        TardisWaypoint waypoint = new TardisWaypoint(
                UUID.randomUUID(),
                name,
                model.exteriorDimension,
                model.exteriorX,
                model.exteriorY,
                model.exteriorZ,
                model.exteriorRotation
        );
        waypoints.add(waypoint);
        model.setChanged();
        return Optional.of(waypoint);
    }

    public static boolean delete(@Nullable TardisDataModel model, @Nullable UUID waypointId) {
        if (model == null || waypointId == null) {
            return false;
        }
        List<TardisWaypoint> waypoints = model.getWaypoints();
        boolean removed = waypoints.removeIf(w -> w != null && waypointId.equals(w.id));
        if (!removed) {
            return false;
        }
        if (waypointId.equals(model.selectedWaypointId)) {
            model.selectedWaypointId = null;
            if (model.getDestinationMode() == DestinationMode.WAYPOINT) {
                model.setDestinationMode(DestinationMode.BIOME);
            }
        }
        model.setChanged();
        return true;
    }

    /**
     * Renames an existing waypoint. Blank names and duplicates (case-insensitive) are rejected.
     *
     * @return true when the name was applied
     */
    public static boolean rename(
            @Nullable TardisDataModel model,
            @Nullable UUID waypointId,
            @Nullable String requestedName
    ) {
        if (model == null || waypointId == null || requestedName == null || requestedName.isBlank()) {
            return false;
        }
        Optional<TardisWaypoint> existing = find(model, waypointId);
        if (existing.isEmpty()) {
            return false;
        }
        String name = requestedName.trim();
        if (!isNameUnique(model, name, waypointId)) {
            return false;
        }
        existing.get().name = name;
        model.setChanged();
        return true;
    }

    /**
     * Selects a waypoint as the travel destination and switches mode to {@link DestinationMode#WAYPOINT}.
     */
    public static boolean select(@Nullable TardisDataModel model, @Nullable UUID waypointId) {
        if (model == null || waypointId == null) {
            return false;
        }
        if (find(model, waypointId).isEmpty()) {
            return false;
        }
        model.selectedWaypointId = waypointId;
        model.selectedPlayerUuid = null;
        model.setDestinationMode(DestinationMode.WAYPOINT);
        return true;
    }

    /**
     * Clears waypoint/player destination selection and resets mode to {@link DestinationMode#BIOME}.
     */
    public static boolean clearSelection(@Nullable TardisDataModel model) {
        if (model == null) {
            return false;
        }
        model.clearNonBiomeDestinationSelection();
        return true;
    }

    /**
     * Finds the first waypoint that matches the model's current exterior dimension and block position.
     */
    public static Optional<TardisWaypoint> findAtExterior(@Nullable TardisDataModel model) {
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null
                || model.exteriorDimension.isBlank()) {
            return Optional.empty();
        }
        for (TardisWaypoint waypoint : model.getWaypoints()) {
            if (waypoint == null || waypoint.dimension == null) {
                continue;
            }
            if (model.exteriorDimension.equals(waypoint.dimension)
                    && model.exteriorX == waypoint.x
                    && model.exteriorY == waypoint.y
                    && model.exteriorZ == waypoint.z) {
                return Optional.of(waypoint);
            }
        }
        return Optional.empty();
    }

    static String nextGeneratedName(TardisDataModel model) {
        int index = 1;
        while (true) {
            String candidate = "Waypoint " + index;
            if (isNameUnique(model, candidate)) {
                return candidate;
            }
            index++;
        }
    }

    /** Normalizes user input for comparison (trim + case-fold). */
    public static String normalizeName(@Nullable String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
