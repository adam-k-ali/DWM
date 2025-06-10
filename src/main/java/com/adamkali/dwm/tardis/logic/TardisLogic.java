package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;

import java.util.UUID;

public class TardisLogic {
    public static void toggleDoor(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }

        tardis.doorState.isOpen = !tardis.doorState.isOpen;
        tardis.markDirty();
    }

    public static TardisDoorState getDoorState(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.doorState;
    }

    public static void updateDoorState(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }
        float doorSwing = tardis.doorState.doorSwing;
        if (tardis.doorState.isOpen) {
            doorSwing = Math.min(doorSwing + 0.05f, 1f);
        } else {
            doorSwing = Math.max(doorSwing - 0.05f, 0f);
        }
        tardis.doorState.doorSwing = doorSwing;
        tardis.markDirty();
    }

    public static void setVariant(UUID tardisId, TardisChameleonVariant variant) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }

        tardis.variant = variant;
        tardis.markDirty();
    }

    public static TardisChameleonVariant getVariant(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.variant;
    }
}
