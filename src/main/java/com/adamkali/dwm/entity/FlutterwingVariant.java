package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public enum FlutterwingVariant {
    BLUE_CRYSTAL("blue_crystal"),
    MADRIGAL("madrigal"),
    SILVERBAND("silverband"),
    WILD_ENDEAVOUR("wild_endeavour");

    private final String id;
    private final Identifier textureLocation;

    FlutterwingVariant(String id) {
        this.id = id;
        this.textureLocation = Identifier.fromNamespaceAndPath(
                DWMReference.MOD_ID,
                "textures/entity/flutterwing/" + id + ".png"
        );
    }

    public String getSerializedName() {
        return id;
    }

    public Identifier textureLocation() {
        return textureLocation;
    }

    public static FlutterwingVariant byId(String id) {
        for (FlutterwingVariant variant : values()) {
            if (variant.id.equals(id)) {
                return variant;
            }
        }
        return BLUE_CRYSTAL;
    }

    public static FlutterwingVariant byOrdinal(int ordinal) {
        FlutterwingVariant[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BLUE_CRYSTAL;
        }
        return values[ordinal];
    }

    public static FlutterwingVariant getRandom(RandomSource random) {
        FlutterwingVariant[] values = values();
        return values[random.nextInt(values.length)];
    }
}
