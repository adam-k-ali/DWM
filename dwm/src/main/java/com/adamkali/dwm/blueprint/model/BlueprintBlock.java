package com.adamkali.dwm.blueprint.model;

import org.jetbrains.annotations.Nullable;

/**
 * Block definition from a blueprint JSON ({@code id} plus optional {@code state.facing}).
 */
public record BlueprintBlock(String id, @Nullable BlueprintBlockState state) {
    public BlueprintBlock(String id) {
        this(id, null);
    }
}
