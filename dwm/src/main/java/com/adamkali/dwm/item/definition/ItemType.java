package com.adamkali.dwm.item.definition;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Java class an item definition instantiates. Behaviour-heavy items stay in Java. */
public enum ItemType implements StringRepresentable {
    ITEM("dwm:item"),
    SWORD("dwm:sword"),
    PICKAXE("dwm:pickaxe"),
    SHOVEL("dwm:shovel"),
    AXE("dwm:axe"),
    HOE("dwm:hoe"),
    ARMOR("dwm:armor");

    public static final Codec<ItemType> CODEC = StringRepresentable.fromEnum(ItemType::values);

    private final String serializedName;

    ItemType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public boolean needsTool() {
        return this == SWORD || this == PICKAXE || this == SHOVEL || this == AXE || this == HOE;
    }
}
