package com.adamkali.dwm.blueprint.model;

/**
 * Circle (disk or ring) in the XZ plane at {@code origin.y}.
 */
public record CircleShape(
        BlueprintLocation origin,
        BlueprintBlock block,
        boolean filled,
        double radius
) implements BlueprintShape {
}
