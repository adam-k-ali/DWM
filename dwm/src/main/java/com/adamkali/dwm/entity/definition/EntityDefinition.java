package com.adamkali.dwm.entity.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed datapack-shaped entity definition under {@code data/<ns>/entity/definition/}.
 */
public record EntityDefinition(
        Identifier entity,
        EntityAttributeBase attributeBase,
        Map<Identifier, Double> attributes,
        Map<String, Map<Identifier, Double>> attributeOverrides,
        List<EntityGoalBinding> goals,
        List<EntityGoalBinding> targets
) {
    public static final Codec<EntityDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("entity").forGetter(EntityDefinition::entity),
            EntityAttributeBase.CODEC.fieldOf("attribute_base").forGetter(EntityDefinition::attributeBase),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).fieldOf("attributes").forGetter(EntityDefinition::attributes),
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE))
                    .optionalFieldOf("attribute_overrides", Map.of())
                    .forGetter(EntityDefinition::attributeOverrides),
            EntityGoalBinding.CODEC.listOf().fieldOf("goals").forGetter(EntityDefinition::goals),
            EntityGoalBinding.CODEC.listOf().optionalFieldOf("targets", List.of()).forGetter(EntityDefinition::targets)
    ).apply(instance, EntityDefinition::new));

    public EntityDefinition {
        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("attributes must not be empty for " + entity);
        }
        attributes = Map.copyOf(attributes);
        Map<String, Map<Identifier, Double>> copiedOverrides = new LinkedHashMap<>();
        attributeOverrides.forEach((name, values) -> copiedOverrides.put(name, Map.copyOf(values)));
        attributeOverrides = Map.copyOf(copiedOverrides);
        goals = List.copyOf(goals);
        targets = List.copyOf(targets);
    }
}
