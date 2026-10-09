package com.adamkali.dwm.model.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final Codec<String> NAME_CODEC = Codec.STRING.validate(name ->
            name.isBlank() ? DataResult.error(() -> "part name is required") : DataResult.success(name));

    public static final Codec<EntityModelPart> CODEC = Codec.recursive(
            "EntityModelPart",
            self -> RecordCodecBuilder.create(instance -> instance.group(
                    NAME_CODEC.fieldOf("name").forGetter(EntityModelPart::name),
                    EntityModelVec3.CODEC.optionalFieldOf("pivot", EntityModelVec3.ZERO)
                            .forGetter(EntityModelPart::pivot),
                    EntityModelVec3.CODEC.optionalFieldOf("rotation", EntityModelVec3.ZERO)
                            .forGetter(EntityModelPart::rotation),
                    EntityModelCube.CODEC.listOf().optionalFieldOf("cubes", List.of())
                            .forGetter(EntityModelPart::cubes),
                    uniqueNames(self.listOf()).optionalFieldOf("children", List.of())
                            .forGetter(EntityModelPart::children)
            ).apply(instance, EntityModelPart::new))
    );

    public EntityModelPart {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("part name is required");
        }
        cubes = List.copyOf(cubes);
        children = List.copyOf(children);
        requireUniqueNames(children).getOrThrow(IllegalArgumentException::new);
    }

    /** Wraps a part-list codec so duplicate sibling names are a {@link DataResult} error. */
    static Codec<List<EntityModelPart>> uniqueNames(Codec<List<EntityModelPart>> codec) {
        return codec.validate(EntityModelPart::requireUniqueNames);
    }

    static DataResult<List<EntityModelPart>> requireUniqueNames(List<EntityModelPart> parts) {
        Set<String> seen = new HashSet<>();
        for (EntityModelPart part : parts) {
            if (!seen.add(part.name())) {
                return DataResult.error(() -> "duplicate part name among siblings: " + part.name());
            }
        }
        return DataResult.success(parts);
    }
}
