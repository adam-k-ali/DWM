package com.adamkali.dwm.block.definition;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;

/** Java class a block definition instantiates. Behaviour-heavy blocks stay in Java and are not listed here. */
public enum BlockType implements StringRepresentable {
    BLOCK("dwm:block"),
    STAIRS("dwm:stairs"),
    SLAB("dwm:slab"),
    WALL("dwm:wall"),
    COLORED_FALLING("dwm:colored_falling");

    public static final Codec<BlockType> CODEC = StringRepresentable.fromEnum(BlockType::values);

    private final String serializedName;

    BlockType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /** Types that copy another block's properties and need a {@code base}. */
    public boolean needsBase() {
        return this == STAIRS || this == SLAB || this == WALL;
    }

    public static String names() {
        return Arrays.stream(values()).map(BlockType::getSerializedName).toList().toString();
    }
}
