package com.adamkali.dwm.model.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * One JSON entity mesh file under {@code assets/<ns>/models/entity/}.
 */
public record EntityModelFile(
        Optional<Identifier> parent,
        Optional<Integer> textureWidth,
        Optional<Integer> textureHeight,
        Optional<Identifier> texture,
        List<EntityModelPart> parts
) {
    public static final Codec<EntityModelFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(EntityModelFile::parent),
            Codec.INT.optionalFieldOf("texture_width").forGetter(EntityModelFile::textureWidth),
            Codec.INT.optionalFieldOf("texture_height").forGetter(EntityModelFile::textureHeight),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(EntityModelFile::texture),
            EntityModelPart.CODEC.listOf().optionalFieldOf("parts", List.of()).forGetter(EntityModelFile::parts)
    ).apply(instance, EntityModelFile::new));

    public EntityModelFile {
        parts = List.copyOf(parts);
        textureWidth.ifPresent(value -> {
            if (value < 1) {
                throw new IllegalArgumentException("texture_width must be at least 1");
            }
        });
        textureHeight.ifPresent(value -> {
            if (value < 1) {
                throw new IllegalArgumentException("texture_height must be at least 1");
            }
        });
    }
}
