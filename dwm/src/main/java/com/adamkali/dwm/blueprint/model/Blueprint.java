package com.adamkali.dwm.blueprint.model;

import java.util.List;

/**
 * A blueprint: ordered list of shapes. Later shapes overwrite earlier ones at the same voxel.
 */
public record Blueprint(List<BlueprintShape> shapes) {
    public Blueprint {
        shapes = List.copyOf(shapes);
    }
}
