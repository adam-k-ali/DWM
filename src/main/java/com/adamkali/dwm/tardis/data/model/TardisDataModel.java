package com.adamkali.dwm.tardis.data.model;


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

    /** Current exterior travel phase name ({@link TardisTravelPhase}). */
    public String travelPhase = TardisTravelPhase.IDLE.name();

    /** Countdown ticks remaining within the current travel phase (used by dematerialising hold). */
    public int travelPhaseTicks;

    /** Biome id snapshotted when travel starts; mid-flight biome cycling is ignored. */
    public String travelDestinationBiome;

    /** Dimension id snapshotted when travel starts; mid-flight dimension cycling is ignored. */
    public String travelDestinationDimension;

    private transient boolean needsSaving = false;

    public TardisDataModel() {
        this.uuid = UUID.randomUUID();
        this.doorState = new TardisDoorState();
        this.variant = TardisChameleonVariant.TT_CAPSULE;
        this.travelPhase = TardisTravelPhase.IDLE.name();
    }

    public TardisTravelPhase getTravelPhase() {
        return TardisTravelPhase.fromString(travelPhase);
    }

    public void setTravelPhase(TardisTravelPhase phase) {
        this.travelPhase = phase == null ? TardisTravelPhase.IDLE.name() : phase.name();
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
                + ", travelPhase=" + travelPhase + ", travelPhaseTicks=" + travelPhaseTicks
                + ", travelDestinationBiome=" + travelDestinationBiome
                + ", travelDestinationDimension=" + travelDestinationDimension + ']';
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
                    && Objects.equals(this.travelPhase, other.travelPhase)
                    && this.travelPhaseTicks == other.travelPhaseTicks
                    && Objects.equals(this.travelDestinationBiome, other.travelDestinationBiome)
                    && Objects.equals(this.travelDestinationDimension, other.travelDestinationDimension);
        }
        return false;
    }
}
