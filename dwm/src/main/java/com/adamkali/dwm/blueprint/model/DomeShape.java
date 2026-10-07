package com.adamkali.dwm.blueprint.model;

/**
 * Upper hemisphere with base center at {@code origin}.
 */
public record DomeShape(
        BlueprintLocation origin,
        BlueprintBlock block,
        boolean fill,
        double radius
) implements BlueprintShape {
}
