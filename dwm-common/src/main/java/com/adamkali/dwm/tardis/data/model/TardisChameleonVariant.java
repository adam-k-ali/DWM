package com.adamkali.dwm.tardis.data.model;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;

public enum TardisChameleonVariant {
    TT_CAPSULE(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tt_capsule"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -7.5f)),
    FIRST_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "first_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f)),
    SECOND_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "second_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -6.0f)),
    THIRD_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "third_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f)),
    FOURTH_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "fourth_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f)),
    FIFTH_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "fifth_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f)),
    SIXTH_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sixth_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f)),
    SEVENTH_DOCTOR_BOX(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "seventh_doctor_box"), PortalAperture.ofPixels(-5.0f, 5.0f, 1.0f, 23.0f, -5.5f));

    private final Identifier id;
    private final PortalAperture aperture;

    TardisChameleonVariant(Identifier id, PortalAperture aperture) {
        this.id = id;
        this.aperture = aperture;
    }

    public Identifier getId() {
        return id;
    }

    public PortalAperture getAperture() {
        return aperture;
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
