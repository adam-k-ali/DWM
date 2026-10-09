package com.adamkali.dwm.model.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * One {@code addBox} cuboid on a part.
 */
public record EntityModelCube(
        EntityModelUv uv,
        EntityModelVec3 origin,
        EntityModelVec3 size,
        float inflate,
        boolean mirror
) {
    public static final Codec<EntityModelCube> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityModelUv.CODEC.fieldOf("uv").forGetter(EntityModelCube::uv),
            EntityModelVec3.CODEC.fieldOf("origin").forGetter(EntityModelCube::origin),
            EntityModelVec3.CODEC.fieldOf("size").forGetter(EntityModelCube::size),
            Codec.FLOAT.optionalFieldOf("inflate", 0.0F).forGetter(EntityModelCube::inflate),
            Codec.BOOL.optionalFieldOf("mirror", false).forGetter(EntityModelCube::mirror)
    ).apply(instance, EntityModelCube::new));
}
