package com.adamkali.dwm.blueprint.model;

/**
 * Regular polygon in the XZ plane (5–12 sides).
 */
public record PolygonShape(
        int nSides,
        BlueprintLocation origin,
        BlueprintBlock block,
        boolean fill,
        double radius
) implements BlueprintShape {
}
