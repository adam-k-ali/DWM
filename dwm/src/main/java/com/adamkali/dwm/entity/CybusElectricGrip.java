package com.adamkali.dwm.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Cybus Cyberman electric grip: every landed melee hit electrocutes the target, slowing it briefly.
 */
public final class CybusElectricGrip {
    /** Slowness length after a grip, in ticks (2 seconds). */
    public static final int SLOWNESS_TICKS = 40;
    /** Slowness amplifier (1 = Slowness II). */
    public static final int SLOWNESS_AMPLIFIER = 1;
    /** Electric spark particles spawned around the target per grip. */
    public static final int SPARK_COUNT = 12;

    private CybusElectricGrip() {
    }

    public static MobEffectInstance slowness() {
        return new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER);
    }
}
