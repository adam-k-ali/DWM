package com.adamkali.dwm.tardis.data.model;

/**
 * Exterior BOTI door aperture in model-local space after BER transforms
 * (Blockbench units / 16).
 */
public record TardisBotiAperture(float x0, float x1, float y0, float y1, float z) {
    public static TardisBotiAperture ofPixels(float x0, float x1, float y0, float y1, float z) {
        return new TardisBotiAperture(x0 / 16.0f, x1 / 16.0f, y0 / 16.0f, y1 / 16.0f, z / 16.0f);
    }

    public float centerY() {
        return (y0 + y1) * 0.5f;
    }
}
