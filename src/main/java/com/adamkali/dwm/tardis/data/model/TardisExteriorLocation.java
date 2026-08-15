package com.adamkali.dwm.tardis.data.model;

import java.util.Objects;

/**
 * Snapshot of an exterior landing (dimension + block coords + facing).
 * Used by fast-return history; not a named waypoint.
 */
public class TardisExteriorLocation {
    /** Registry key id of the exterior dimension, e.g. {@code minecraft:overworld}. */
    public String dimension;
    public int x;
    public int y;
    public int z;
    public int rotation;

    public TardisExteriorLocation() {
    }

    public TardisExteriorLocation(String dimension, int x, int y, int z, int rotation) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    /** Captures the model's current exterior when {@link TardisDataModel#hasExteriorLocation} is set. */
    public static TardisExteriorLocation fromModel(TardisDataModel model) {
        return new TardisExteriorLocation(
                model.exteriorDimension,
                model.exteriorX,
                model.exteriorY,
                model.exteriorZ,
                model.exteriorRotation
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TardisExteriorLocation other) {
            return Objects.equals(this.dimension, other.dimension)
                    && this.x == other.x
                    && this.y == other.y
                    && this.z == other.z
                    && this.rotation == other.rotation;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, x, y, z, rotation);
    }

    @Override
    public String toString() {
        return "TardisExteriorLocation[dimension=" + dimension
                + ", x=" + x + ", y=" + y + ", z=" + z + ", rotation=" + rotation + ']';
    }
}
