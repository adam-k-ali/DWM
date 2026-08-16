package com.adamkali.dwm.tardis.data.model;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TardisDataModel {
    public UUID uuid;
    public TardisDoorState doorState;
    public TardisChameleonVariant variant;

    /** Registry key id of the exterior dimension, e.g. {@code minecraft:overworld}. */
    public String exteriorDimension;
    public int exteriorX;
    public int exteriorY;
    public int exteriorZ;
    public int exteriorRotation;
    public boolean hasExteriorLocation;

    /** Registry key id of the selected destination biome, e.g. {@code minecraft:plains}. */
    public String selectedBiome;

    /**
     * Registry key id of the selected destination dimension, e.g. {@code minecraft:the_nether}.
     * Null means fall back to {@link #exteriorDimension} for biome listing and travel.
     */
    public String selectedDimension;

    /** How the next landing is resolved. Defaults to {@link DestinationMode#BIOME}. */
    public DestinationMode destinationMode = DestinationMode.BIOME;

    /** Saved exterior waypoints (cap enforced by {@code WaypointLogic}). */
    public List<TardisWaypoint> waypoints = new ArrayList<>();

    /** Selected waypoint id when {@link #destinationMode} is {@link DestinationMode#WAYPOINT}. */
    public @Nullable UUID selectedWaypointId;

    /** Selected player uuid when {@link #destinationMode} is {@link DestinationMode#PLAYER}. */
    public @Nullable UUID selectedPlayerUuid;

    /**
     * LIFO exterior landing history for fast return (cap enforced by {@code FastReturnLogic}).
     * Index 0 is the most recently departed exterior.
     */
    public List<TardisExteriorLocation> locationHistory = new ArrayList<>();

    /** Cursor into {@link #locationHistory} when {@link #destinationMode} is {@link DestinationMode#FAST_RETURN}. */
    public int selectedFastReturnIndex;

    /**
     * When true (default), materialise uses the resolved landing exactly.
     * When false, landing scatters around the resolved destination.
     * Boxed so Gson loads of older saves (missing field) stay default-on via null.
     */
    public Boolean stabilisersEnabled = Boolean.TRUE;

    /** Perception filter: hide the exterior shell/doors/BOTI. Default off. */
    public boolean cloaked;

    /** When true, doors refuse opening. Closing is always allowed. */
    public boolean doorsLocked;

    /** Pin landing X/Y/Z to the current exterior after resolve + scatter. */
    public boolean lockX;
    public boolean lockY;
    public boolean lockZ;

    /** Current exterior travel phase name ({@link TardisTravelPhase}). */
    public String travelPhase = TardisTravelPhase.IDLE.name();

    /** Countdown ticks remaining within the current travel phase (used by dematerialising hold). */
    public int travelPhaseTicks;

    /** Biome id snapshotted when travel starts; mid-flight biome cycling is ignored. */
    public String travelDestinationBiome;

    /** Dimension id snapshotted when travel starts; mid-flight dimension cycling is ignored. */
    public String travelDestinationDimension;

    /** Destination mode snapshotted when travel starts. */
    public @Nullable DestinationMode travelDestinationMode;

    /** Waypoint/fast-return exact coords snapshotted at demat. */
    public int travelDestinationX;
    public int travelDestinationY;
    public int travelDestinationZ;
    public int travelDestinationRotation;

    /** Player uuid snapshotted at demat (meaningful for {@link DestinationMode#PLAYER}). */
    public @Nullable UUID travelTargetPlayerUuid;

    private transient boolean needsSaving = false;

    public TardisDataModel() {
        this.uuid = UUID.randomUUID();
        this.doorState = new TardisDoorState();
        this.variant = TardisChameleonVariant.TT_CAPSULE;
        this.travelPhase = TardisTravelPhase.IDLE.name();
        this.destinationMode = DestinationMode.BIOME;
        this.waypoints = new ArrayList<>();
        this.locationHistory = new ArrayList<>();
        this.selectedFastReturnIndex = 0;
        this.stabilisersEnabled = Boolean.TRUE;
    }

    public TardisTravelPhase getTravelPhase() {
        return TardisTravelPhase.fromString(travelPhase);
    }

    public void setTravelPhase(TardisTravelPhase phase) {
        this.travelPhase = phase == null ? TardisTravelPhase.IDLE.name() : phase.name();
        setChanged();
    }

    public DestinationMode getDestinationMode() {
        return destinationMode == null ? DestinationMode.BIOME : destinationMode;
    }

    public void setDestinationMode(DestinationMode mode) {
        this.destinationMode = mode == null ? DestinationMode.BIOME : mode;
        setChanged();
    }

    /**
     * Ensures {@link #waypoints} is non-null after Gson load of older saves.
     */
    public List<TardisWaypoint> getWaypoints() {
        if (waypoints == null) {
            waypoints = new ArrayList<>();
        }
        return waypoints;
    }

    /**
     * Ensures {@link #locationHistory} is non-null after Gson load of older saves.
     */
    public List<TardisExteriorLocation> getLocationHistory() {
        if (locationHistory == null) {
            locationHistory = new ArrayList<>();
        }
        return locationHistory;
    }

    /**
     * Clears waypoint/player/fast-return selection and resets mode to {@link DestinationMode#BIOME}.
     */
    public void clearNonBiomeDestinationSelection() {
        this.destinationMode = DestinationMode.BIOME;
        this.selectedWaypointId = null;
        this.selectedPlayerUuid = null;
        this.selectedFastReturnIndex = 0;
        setChanged();
    }

    /** Clears flight snapshot fields used by materialise resolution. */
    public void clearTravelDestinationSnapshot() {
        this.travelDestinationBiome = null;
        this.travelDestinationDimension = null;
        this.travelDestinationMode = null;
        this.travelDestinationX = 0;
        this.travelDestinationY = 0;
        this.travelDestinationZ = 0;
        this.travelDestinationRotation = 0;
        this.travelTargetPlayerUuid = null;
        setChanged();
    }

    public boolean needsSaving() {
        return needsSaving;
    }

    public void setChanged() {
        this.needsSaving = true;
    }

    public void setExteriorLocation(String dimensionId, int x, int y, int z, int rotation) {
        this.exteriorDimension = dimensionId;
        this.exteriorX = x;
        this.exteriorY = y;
        this.exteriorZ = z;
        this.exteriorRotation = rotation;
        this.hasExteriorLocation = true;
        setChanged();
    }

    @Override
    public String toString() {
        return "TardisDataModel [uuid=" + uuid + ", doorState=" + doorState + ", variant=" + variant
                + ", exteriorDimension=" + exteriorDimension + ", hasExteriorLocation=" + hasExteriorLocation
                + ", selectedBiome=" + selectedBiome
                + ", selectedDimension=" + selectedDimension
                + ", destinationMode=" + destinationMode
                + ", waypoints=" + (waypoints == null ? 0 : waypoints.size())
                + ", selectedWaypointId=" + selectedWaypointId
                + ", selectedPlayerUuid=" + selectedPlayerUuid
                + ", locationHistory=" + (locationHistory == null ? 0 : locationHistory.size())
                + ", selectedFastReturnIndex=" + selectedFastReturnIndex
                + ", stabilisersEnabled=" + stabilisersEnabled
                + ", cloaked=" + cloaked
                + ", doorsLocked=" + doorsLocked
                + ", lockX=" + lockX + ", lockY=" + lockY + ", lockZ=" + lockZ
                + ", travelPhase=" + travelPhase + ", travelPhaseTicks=" + travelPhaseTicks
                + ", travelDestinationBiome=" + travelDestinationBiome
                + ", travelDestinationDimension=" + travelDestinationDimension
                + ", travelDestinationMode=" + travelDestinationMode + ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TardisDataModel other) {
            return this.uuid.equals(other.uuid)
                    && this.doorState.equals(other.doorState)
                    && this.variant.equals(other.variant)
                    && Objects.equals(this.exteriorDimension, other.exteriorDimension)
                    && this.exteriorX == other.exteriorX
                    && this.exteriorY == other.exteriorY
                    && this.exteriorZ == other.exteriorZ
                    && this.exteriorRotation == other.exteriorRotation
                    && this.hasExteriorLocation == other.hasExteriorLocation
                    && Objects.equals(this.selectedBiome, other.selectedBiome)
                    && Objects.equals(this.selectedDimension, other.selectedDimension)
                    && this.getDestinationMode() == other.getDestinationMode()
                    && Objects.equals(this.getWaypoints(), other.getWaypoints())
                    && Objects.equals(this.selectedWaypointId, other.selectedWaypointId)
                    && Objects.equals(this.selectedPlayerUuid, other.selectedPlayerUuid)
                    && Objects.equals(this.getLocationHistory(), other.getLocationHistory())
                    && this.selectedFastReturnIndex == other.selectedFastReturnIndex
                    && Objects.equals(this.stabilisersEnabled, other.stabilisersEnabled)
                    && this.cloaked == other.cloaked
                    && this.doorsLocked == other.doorsLocked
                    && this.lockX == other.lockX
                    && this.lockY == other.lockY
                    && this.lockZ == other.lockZ
                    && Objects.equals(this.travelPhase, other.travelPhase)
                    && this.travelPhaseTicks == other.travelPhaseTicks
                    && Objects.equals(this.travelDestinationBiome, other.travelDestinationBiome)
                    && Objects.equals(this.travelDestinationDimension, other.travelDestinationDimension)
                    && Objects.equals(this.travelDestinationMode, other.travelDestinationMode)
                    && this.travelDestinationX == other.travelDestinationX
                    && this.travelDestinationY == other.travelDestinationY
                    && this.travelDestinationZ == other.travelDestinationZ
                    && this.travelDestinationRotation == other.travelDestinationRotation
                    && Objects.equals(this.travelTargetPlayerUuid, other.travelTargetPlayerUuid);
        }
        return false;
    }
}
