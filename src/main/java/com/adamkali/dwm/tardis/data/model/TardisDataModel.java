package com.adamkali.dwm.tardis.data.model;


import java.util.UUID;

public class TardisDataModel {
    public UUID uuid;
    public TardisDoorState doorState;
    public TardisChameleonVariant variant;

    public TardisDataModel() {
        this.uuid = UUID.randomUUID();
        this.doorState = new TardisDoorState();
        this.variant = TardisChameleonVariant.TT_CAPSULE;
    }
}
