package com.adamkali.dwm.blueprint.model;

/**
 * Full sphere around {@code origin}.
 */
public record SphereShape(
        BlueprintLocation origin,
        BlueprintBlock block,
        boolean fill,
        double radius
) implements BlueprintShape {
}
