package com.adamkali.dwm.tardis.portal;

/**
 * Distinguishes exterior look-in (BOTI) from interior look-out (SOTO) portal streams.
 * Wire encoding: ordinal as unsigned byte.
 */
public enum PortalStreamKind {
    BOTI,
    SOTO;

    public byte toWire() {
        return (byte) ordinal();
    }

    public static PortalStreamKind fromWire(byte value) {
        PortalStreamKind[] values = values();
        int index = value & 0xFF;
        if (index < 0 || index >= values.length) {
            throw new IllegalArgumentException("Unknown PortalStreamKind wire value: " + value);
        }
        return values[index];
    }
}
