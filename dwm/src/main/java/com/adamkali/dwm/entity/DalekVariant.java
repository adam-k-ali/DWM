package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public enum DalekVariant {
    CLASSIC_1963("1963", DalekChassis.CLASSIC_1963, 6),
    BRONZE_TIME_WAR("time_war_bronze", DalekChassis.TIME_WAR, 3),
    BRONZE_TIME_WAR_DARK("time_war_bronze_dark", DalekChassis.TIME_WAR, 2),
    PARADIGM_DRONE("paradigm_drone", DalekChassis.PARADIGM, 8),
    PARADIGM_STRATEGIST("paradigm_strategist", DalekChassis.PARADIGM, 4),
    PARADIGM_SCIENTIST("paradigm_scientist", DalekChassis.PARADIGM, 4),
    PARADIGM_ETERNAL("paradigm_eternal", DalekChassis.PARADIGM, 3),
    PARADIGM_SUPREME("paradigm_supreme", DalekChassis.PARADIGM, 1);

    private final String id;
    private final DalekChassis chassis;
    private final int spawnWeight;
    private final Identifier textureLocation;

    DalekVariant(String id, DalekChassis chassis, int spawnWeight) {
        this.id = id;
        this.chassis = chassis;
        this.spawnWeight = spawnWeight;
        this.textureLocation = Identifier.fromNamespaceAndPath(
                DWMReference.MOD_ID,
                "textures/entity/dalek/" + id + ".png"
        );
    }

    public String getSerializedName() {
        return id;
    }

    public DalekChassis chassis() {
        return chassis;
    }

    public DalekFamily family() {
        return chassis.family();
    }

    public Identifier textureLocation() {
        return textureLocation;
    }

    /** Weighted random pick among the variants of {@code family}. */
    public static DalekVariant pick(RandomSource random, DalekFamily family) {
        int total = 0;
        for (DalekVariant variant : values()) {
            if (variant.family() == family) {
                total += variant.spawnWeight;
            }
        }
        int roll = random.nextInt(total);
        for (DalekVariant variant : values()) {
            if (variant.family() != family) {
                continue;
            }
            roll -= variant.spawnWeight;
            if (roll < 0) {
                return variant;
            }
        }
        return family.defaultVariant();
    }

    /** Variant with this id, or the family default when the id is unknown or belongs to another family. */
    public static DalekVariant byId(String id, DalekFamily family) {
        for (DalekVariant variant : values()) {
            if (variant.id.equals(id)) {
                return fitting(variant, family);
            }
        }
        return family.defaultVariant();
    }

    public static DalekVariant byOrdinal(int ordinal, DalekFamily family) {
        DalekVariant[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return family.defaultVariant();
        }
        return fitting(values[ordinal], family);
    }

    public static DalekVariant fitting(DalekVariant variant, DalekFamily family) {
        return variant.family() == family ? variant : family.defaultVariant();
    }
}
