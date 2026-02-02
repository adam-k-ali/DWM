package com.adamkali.dwm.tardis.data.model;


import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class TardisDataModel {
    public UUID uuid;
    public TardisDoorState doorState;
    public TardisChameleonVariant variant;
    public String interiorDimension;
    public BlockPos interiorEntrancePos;

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

    @Override
    public String toString() {
        return "TardisDataModel [uuid=" + uuid + ", doorState=" + doorState + ", variant=" + variant + ", interiorDimension=" + interiorDimension + ", interiorEntrancePos=" + interiorEntrancePos + ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TardisDataModel) {
            TardisDataModel other = (TardisDataModel) obj;
            return this.uuid.equals(other.uuid) && 
                   this.doorState.equals(other.doorState) && 
                   this.variant.equals(other.variant) &&
                   java.util.Objects.equals(this.interiorDimension, other.interiorDimension) &&
                   java.util.Objects.equals(this.interiorEntrancePos, other.interiorEntrancePos);
        }
        return false;
    }
}
