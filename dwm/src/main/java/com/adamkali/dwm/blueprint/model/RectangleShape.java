package com.adamkali.dwm.blueprint.model;

/**
 * Axis-aligned cuboid from {@code from} to {@code to} (inclusive).
 */
public record RectangleShape(
        BlueprintLocation from,
        BlueprintLocation to,
        BlueprintBlock block,
        boolean fill
) implements BlueprintShape {
}
