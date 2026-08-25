package com.adamkali.dwm.tardis.data.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Named exterior landing bookmark stored on a TARDIS.
 */
public class TardisWaypoint {
    public UUID id;
    public String name;
    /** Registry key id of the exterior dimension, e.g. {@code minecraft:overworld}. */
    public String dimension;
    public int x;
    public int y;
    public int z;
    public int rotation;

    public TardisWaypoint() {
        this.id = UUID.randomUUID();
    }

    public TardisWaypoint(UUID id, String name, String dimension, int x, int y, int z, int rotation) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TardisWaypoint other) {
            return Objects.equals(this.id, other.id)
                    && Objects.equals(this.name, other.name)
                    && Objects.equals(this.dimension, other.dimension)
                    && this.x == other.x
                    && this.y == other.y
                    && this.z == other.z
                    && this.rotation == other.rotation;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dimension, x, y, z, rotation);
    }

    @Override
    public String toString() {
        return "TardisWaypoint[id=" + id + ", name=" + name + ", dimension=" + dimension
                + ", x=" + x + ", y=" + y + ", z=" + z + ", rotation=" + rotation + ']';
    }
}
