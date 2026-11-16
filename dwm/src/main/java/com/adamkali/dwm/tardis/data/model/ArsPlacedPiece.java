package com.adamkali.dwm.tardis.data.model;

import java.util.Objects;

/**
 * One Architectural Reconfiguration System piece placed on a TARDIS interior plot grid.
 *
 * <p>The anchor ({@link #cellX}, {@link #cellZ}) is the minimum grid cell of the piece's
 * <em>rotated</em> footprint. {@link #rotation} counts clockwise quarter turns (0..3), in vanilla
 * {@code Rotation} order. See {@code ArsGrid} and {@code ArsLayoutLogic} for the rules.
 */
public class ArsPlacedPiece {
    /** Piece definition id, e.g. {@code dwm:library}. */
    public String pieceId;
    public int cellX;
    public int cellZ;
    public int rotation;

    public ArsPlacedPiece() {
    }

    public ArsPlacedPiece(String pieceId, int cellX, int cellZ, int rotation) {
        this.pieceId = pieceId;
        this.cellX = cellX;
        this.cellZ = cellZ;
        this.rotation = rotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArsPlacedPiece other) {
            return Objects.equals(this.pieceId, other.pieceId)
                    && this.cellX == other.cellX
                    && this.cellZ == other.cellZ
                    && this.rotation == other.rotation;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceId, cellX, cellZ, rotation);
    }

    @Override
    public String toString() {
        return "ArsPlacedPiece[pieceId=" + pieceId + ", cellX=" + cellX + ", cellZ=" + cellZ
                + ", rotation=" + rotation + ']';
    }
}
