package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public enum ThalVariant {
    THAL_1("thal_1"),
    THAL_2("thal_2"),
    THAL_3("thal_3"),
    THAL_4("thal_4");

    private final String id;
    private final Identifier textureLocation;

    ThalVariant(String id) {
        this.id = id;
        this.textureLocation = Identifier.fromNamespaceAndPath(
                DWMReference.MOD_ID,
                "textures/entity/thal/" + id + ".png"
        );
    }

    public String getSerializedName() {
        return id;
    }

    public Identifier textureLocation() {
        return textureLocation;
    }

    public static ThalVariant byId(String id) {
        for (ThalVariant variant : values()) {
            if (variant.id.equals(id)) {
                return variant;
            }
        }
        return THAL_1;
    }

    public static ThalVariant byOrdinal(int ordinal) {
        ThalVariant[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return THAL_1;
        }
        return values[ordinal];
    }

    public static ThalVariant getRandom(RandomSource random) {
        ThalVariant[] values = values();
        return values[random.nextInt(values.length)];
    }
}
