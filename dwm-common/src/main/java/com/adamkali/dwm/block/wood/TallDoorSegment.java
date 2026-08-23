package com.adamkali.dwm.block.wood;

import net.minecraft.util.StringRepresentable;

public enum TallDoorSegment implements StringRepresentable {
    BOTTOM("bottom", 0),
    MIDDLE("middle", 1),
    TOP("top", 2);

    private final String name;
    private final int index;

    TallDoorSegment(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public int index() {
        return index;
    }

    public static TallDoorSegment fromIndex(int index) {
        return switch (index) {
            case 1 -> MIDDLE;
            case 2 -> TOP;
            default -> BOTTOM;
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
