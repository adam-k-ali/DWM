package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public enum DalekVariant {
    CLASSIC_1963("1963", 6),
    BRONZE_TIME_WAR("time_war_bronze", 3),
    BRONZE_TIME_WAR_DARK("time_war_bronze_dark", 2);

    private final String id;
    private final int spawnWeight;
    private final Identifier textureLocation;

    DalekVariant(String id, int spawnWeight) {
        this.id = id;
        this.spawnWeight = spawnWeight;
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

    public static DalekVariant pick(RandomSource random) {
        int total = 0;
        for (DalekVariant variant : values()) {
            total += variant.spawnWeight;
        }
        int roll = random.nextInt(total);
        for (DalekVariant variant : values()) {
            roll -= variant.spawnWeight;
            if (roll < 0) {
                return variant;
            }
        }
        return CLASSIC_1963;
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
