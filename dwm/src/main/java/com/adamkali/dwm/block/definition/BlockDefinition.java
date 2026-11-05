package com.adamkali.dwm.block.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Parsed block definition under {@code data/<ns>/block/definition/}.
 *
 * @param block             block id; must match the file stem
 * @param type              Java class to instantiate
 * @param base              block whose properties/state a stairs, slab or wall copies (must already be registered)
 * @param fallingDustColor  RGB dust colour for {@link BlockType#COLORED_FALLING}
 * @param item              whether a {@code BlockItem} is registered alongside the block (default {@code true})
 * @param creativeTab       optional vanilla creative tab id for definitions registered by the data pass
 * @param properties        block settings
 */
public record BlockDefinition(
        Identifier block,
        BlockType type,
        Optional<Identifier> base,
        Optional<Integer> fallingDustColor,
        boolean item,
        Optional<Identifier> creativeTab,
        BlockProperties properties
) {
    public static final Codec<BlockDefinition> CODEC = RecordCodecBuilder.<BlockDefinition>create(instance -> instance.group(
            Identifier.CODEC.fieldOf("block").forGetter(BlockDefinition::block),
            BlockType.CODEC.optionalFieldOf("type", BlockType.BLOCK).forGetter(BlockDefinition::type),
            Identifier.CODEC.optionalFieldOf("base").forGetter(BlockDefinition::base),
            Codec.INT.optionalFieldOf("falling_dust_color").forGetter(BlockDefinition::fallingDustColor),
            Codec.BOOL.optionalFieldOf("item", true).forGetter(BlockDefinition::item),
            Identifier.CODEC.optionalFieldOf("creative_tab").forGetter(BlockDefinition::creativeTab),
            BlockProperties.CODEC.optionalFieldOf("properties", BlockProperties.EMPTY).forGetter(BlockDefinition::properties)
    ).apply(instance, BlockDefinition::new)).validate(definition -> {
        if (definition.type().needsBase() && definition.base().isEmpty()) {
            return com.mojang.serialization.DataResult.error(() -> definition.block() + ": type " + definition.type().getSerializedName() + " requires 'base'");
        }
        if (definition.type() == BlockType.COLORED_FALLING && definition.fallingDustColor().isEmpty()) {
            return com.mojang.serialization.DataResult.error(() -> definition.block() + ": type dwm:colored_falling requires 'falling_dust_color'");
        }
        return com.mojang.serialization.DataResult.success(definition);
    });
}
