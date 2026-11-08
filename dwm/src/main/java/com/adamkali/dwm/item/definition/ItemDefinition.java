package com.adamkali.dwm.item.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * Parsed item definition under {@code data/<ns>/item/definition/}.
 *
 * @param item       item id; must match the file stem
 * @param type       Java class to instantiate; tool types require {@code properties.tool}, {@code dwm:armor} requires {@code properties.armor}
 * @param properties item settings
 */
public record ItemDefinition(Identifier item, ItemType type, ItemProperties properties) {
    public static final Codec<ItemDefinition> CODEC = RecordCodecBuilder.<ItemDefinition>create(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(ItemDefinition::item),
            ItemType.CODEC.optionalFieldOf("type", ItemType.ITEM).forGetter(ItemDefinition::type),
            ItemProperties.CODEC.optionalFieldOf("properties", ItemProperties.EMPTY).forGetter(ItemDefinition::properties)
    ).apply(instance, ItemDefinition::new)).validate(definition -> {
        if (definition.type().needsTool() && definition.properties().tool().isEmpty()) {
            return DataResult.error(() -> definition.item() + ": type " + definition.type().getSerializedName() + " requires properties.tool");
        }
        if (definition.type() == ItemType.ARMOR && definition.properties().armor().isEmpty()) {
            return DataResult.error(() -> definition.item() + ": type dwm:armor requires properties.armor");
        }
        return DataResult.success(definition);
    });
}
