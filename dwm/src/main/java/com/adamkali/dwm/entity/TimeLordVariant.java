package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public enum TimeLordVariant {
    TIME_LORD_1("time_lord_1"),
    TIME_LORD_2("time_lord_2"),
    TIME_LORD_3("time_lord_3"),
    TIME_LORD_4("time_lord_4");

    private final String id;
    private final Identifier textureLocation;

    TimeLordVariant(String id) {
        this.id = id;
        this.textureLocation = Identifier.fromNamespaceAndPath(
                DWMReference.MOD_ID,
                "textures/entity/" + id + ".png"
        );
    }

    public String getSerializedName() {
        return id;
    }

    public Identifier textureLocation() {
        return textureLocation;
    }

    public static TimeLordVariant byId(String id) {
        for (TimeLordVariant variant : values()) {
            if (variant.id.equals(id)) {
                return variant;
            }
        }
        return TIME_LORD_1;
    }

    public static TimeLordVariant byOrdinal(int ordinal) {
        TimeLordVariant[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return TIME_LORD_1;
        }
        return values[ordinal];
    }

    public static TimeLordVariant getRandom(RandomSource random) {
        TimeLordVariant[] values = values();
        return values[random.nextInt(values.length)];
    }
}
