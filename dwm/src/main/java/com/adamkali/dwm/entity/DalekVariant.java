package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;

public enum DalekVariant {
    CLASSIC_1963("1963");

    private final String id;
    private final Identifier textureLocation;

    DalekVariant(String id) {
        this.id = id;
        this.textureLocation = Identifier.fromNamespaceAndPath(
                DWMReference.MOD_ID,
                "textures/entity/dalek/" + id + ".png"
        );
    }

    public String getSerializedName() {
        return id;
    }

    public Identifier textureLocation() {
        return textureLocation;
    }

    public static DalekVariant byId(String id) {
        for (DalekVariant variant : values()) {
            if (variant.id.equals(id)) {
                return variant;
            }
        }
        return CLASSIC_1963;
    }

    public static DalekVariant byOrdinal(int ordinal) {
        DalekVariant[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return CLASSIC_1963;
        }
        return values[ordinal];
    }
}
