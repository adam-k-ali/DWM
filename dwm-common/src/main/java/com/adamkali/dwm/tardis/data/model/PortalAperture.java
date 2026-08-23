package com.adamkali.dwm.tardis.data.model;

/**
 * Door aperture in model-local space after BER transforms (Blockbench units / 16).
 * Shared by exterior BOTI (per-chameleon) and interior SOTO (classic opening).
 */
public record PortalAperture(float x0, float x1, float y0, float y1, float z) {
    /**
     * Full classic interior double-door opening (doors + clear span), matching
     * {@code TardisInteriorDoorShapes} mesh extents. Z matches {@code MODEL_MIN_Z_PX}
     * (room-facing door plane after BER X-180).
     */
    public static final PortalAperture CLASSIC_INTERIOR_DOORS =
            ofPixels(-16.0f, 32.0f, 0.0f, 32.0f, -2.0f);

    public static PortalAperture ofPixels(float x0, float x1, float y0, float y1, float z) {
        return new PortalAperture(x0 / 16.0f, x1 / 16.0f, y0 / 16.0f, y1 / 16.0f, z / 16.0f);
    }

    public float centerX() {
        return (x0 + x1) * 0.5f;
    }

    public float centerY() {
        return (y0 + y1) * 0.5f;
    }
}
