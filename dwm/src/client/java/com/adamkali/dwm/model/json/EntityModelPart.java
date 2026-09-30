package com.adamkali.dwm.model.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Named bone: optional cubes, pivot, rest rotation in degrees, and children.
 */
public record EntityModelPart(
        String name,
        EntityModelVec3 pivot,
        EntityModelVec3 rotation,
        List<EntityModelCube> cubes,
        List<EntityModelPart> children
) {
    public static final Codec<EntityModelPart> CODEC = Codec.recursive(
            "EntityModelPart",
            self -> RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(EntityModelPart::name),
                    EntityModelVec3.CODEC.optionalFieldOf("pivot", EntityModelVec3.ZERO)
                            .forGetter(EntityModelPart::pivot),
                    EntityModelVec3.CODEC.optionalFieldOf("rotation", EntityModelVec3.ZERO)
                            .forGetter(EntityModelPart::rotation),
                    EntityModelCube.CODEC.listOf().optionalFieldOf("cubes", List.of())
                            .forGetter(EntityModelPart::cubes),
                    self.listOf().optionalFieldOf("children", List.of())
                            .forGetter(EntityModelPart::children)
            ).apply(instance, EntityModelPart::new))
    );

    public EntityModelPart {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("part name is required");
        }
        cubes = List.copyOf(cubes);
        children = List.copyOf(children);
    }
}
