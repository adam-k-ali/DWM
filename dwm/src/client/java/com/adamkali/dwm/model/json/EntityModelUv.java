package com.adamkali.dwm.model.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.List;

/**
 * {@code texOffs(u, v)} atlas origin for a cube.
 */
public record EntityModelUv(int u, int v) {
    public static final Codec<EntityModelUv> CODEC = Codec.INT.listOf().comapFlatMap(
            list -> {
                if (list.size() != 2) {
                    return DataResult.error(() -> "Expected 2 integers, got " + list.size());
                }
                return DataResult.success(new EntityModelUv(list.get(0), list.get(1)));
            },
            uv -> List.of(uv.u(), uv.v())
    );
}
