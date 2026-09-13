package com.adamkali.dwm.blueprint.model;

/**
 * Sealed hierarchy of blueprint shapes. Discriminated by JSON {@code type}.
 */
public sealed interface BlueprintShape
        permits CircleShape, RectangleShape, SphereShape, PolygonShape, DomeShape {
    BlueprintBlock block();
}
