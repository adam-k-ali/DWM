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

    private transient boolean needsSaving = false;

    public TardisDataModel() {
        this.uuid = UUID.randomUUID();
        this.doorState = new TardisDoorState();
        this.variant = TardisChameleonVariant.TT_CAPSULE;
    }

    public boolean needsSaving() {
        return needsSaving;
    }

    public void markDirty() {
        this.needsSaving = true;
    }

    public void setExteriorLocation(String dimensionId, int x, int y, int z, int rotation) {
        this.exteriorDimension = dimensionId;
        this.exteriorX = x;
        this.exteriorY = y;
        this.exteriorZ = z;
        this.exteriorRotation = rotation;
        this.hasExteriorLocation = true;
        markDirty();
    }

    @Override
    public String toString() {
        return "TardisDataModel [uuid=" + uuid + ", doorState=" + doorState + ", variant=" + variant
                + ", exteriorDimension=" + exteriorDimension + ", hasExteriorLocation=" + hasExteriorLocation + ']';
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
                    && this.hasExteriorLocation == other.hasExteriorLocation;
        }
        return false;
    }
}
