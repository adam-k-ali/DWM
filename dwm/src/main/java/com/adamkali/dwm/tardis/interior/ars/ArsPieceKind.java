package com.adamkali.dwm.tardis.interior.ars;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Whether an ARS piece is a room or a corridor. Serialised lowercase ({@code room}, {@code corridor}).
 */
public enum ArsPieceKind implements StringRepresentable {
    ROOM("room"),
    CORRIDOR("corridor");

    public static final Codec<ArsPieceKind> CODEC = StringRepresentable.fromEnum(ArsPieceKind::values);

    private final String serializedName;

    ArsPieceKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
