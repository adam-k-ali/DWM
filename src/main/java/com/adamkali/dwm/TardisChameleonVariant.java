package com.adamkali.dwm;

import net.minecraft.util.Identifier;

public enum TardisChameleonVariant {
    TT_CAPSULE(Identifier.of(DWMReference.MOD_ID, "tt_capsule")),
    FIRST_DOCTOR_BOX(Identifier.of(DWMReference.MOD_ID, "first_doctor_box")),
    SECOND_DOCTOR_BOX(Identifier.of(DWMReference.MOD_ID, "second_doctor_box")),
    THIRD_DOCTOR_BOX(Identifier.of(DWMReference.MOD_ID, "third_doctor_box")),
    FOURTH_DOCTOR_BOX(Identifier.of(DWMReference.MOD_ID, "fourth_doctor_box")),
    FIFTH_DOCTOR_BOX(Identifier.of(DWMReference.MOD_ID, "fifth_doctor_box")),
    SIXTH_DOCTOR_BOX(Identifier.of(DWMReference.MOD_ID, "sixth_doctor_box"));

    private final Identifier id;

    TardisChameleonVariant(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public static TardisChameleonVariant fromId(Identifier id) {
        for (TardisChameleonVariant variant : values()) {
            if (variant.getId().equals(id)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Invalid TardisChameleonVariant ID: " + id);
    }
}
