package com.adamkali.dwm.model.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.List;

/**
 * Three-float vector used for pivot, rest rotation (degrees), cube origin, and size.
 */
public record EntityModelVec3(float x, float y, float z) {
    public static final EntityModelVec3 ZERO = new EntityModelVec3(0.0F, 0.0F, 0.0F);

    public static final Codec<EntityModelVec3> CODEC = Codec.FLOAT.listOf().comapFlatMap(
            list -> {
                if (list.size() != 3) {
                    return DataResult.error(() -> "Expected 3 numbers, got " + list.size());
                }
                return DataResult.success(new EntityModelVec3(list.get(0), list.get(1), list.get(2)));
            },
            vec -> List.of(vec.x(), vec.y(), vec.z())
    );

    public boolean isZero() {
        return x == 0.0F && y == 0.0F && z == 0.0F;
    }
}
