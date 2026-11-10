package com.adamkali.dwm.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Weapons-grade Cyberman adaptive upgrade. Each repeated hit of the same damage type is reduced by a
 * further {@link #STEP} (up to {@link #MAX_REDUCTION}); the first hit of a type always lands in full.
 * A type the Cyberman has not been hit by for {@link #DECAY_TICKS} is forgotten. Damage types are
 * tracked independently, so switching weapons resets the advantage.
 *
 * <p>Pure state: the caller supplies the damage-type key and the game time. Not persisted.
 */
public final class CyberAdaptation {
    /** Extra damage reduction per repeated hit of one type. */
    public static final float STEP = 0.2F;
    /** Largest reduction against one damage type. */
    public static final float MAX_REDUCTION = 0.6F;
    /** Ticks without a hit of a type before its adaptation is forgotten (10 seconds). */
    public static final long DECAY_TICKS = 200L;

    private static final int MAX_STACKS = Math.round(MAX_REDUCTION / STEP);

    private final Map<Object, Entry> entries = new HashMap<>();

    /** Reduction (0 to {@link #MAX_REDUCTION}) the next hit of {@code type} receives at {@code gameTime}. */
    public float reductionFor(Object type, long gameTime) {
        Entry entry = entries.get(type);
        if (entry == null || gameTime - entry.lastHit() > DECAY_TICKS) {
            return 0.0F;
        }
        return Math.min(MAX_REDUCTION, entry.stacks() * STEP);
    }

    /**
     * Scales {@code damage} by the current adaptation against {@code type}, then records the hit.
     *
     * @return the damage that should land
     */
    public float adapt(Object type, long gameTime, float damage) {
        float reduction = reductionFor(type, gameTime);
        recordHit(type, gameTime);
        return damage * (1.0F - reduction);
    }

    /** Records a hit of {@code type}; the next hit of that type is reduced one step further. */
    public void recordHit(Object type, long gameTime) {
        Entry entry = entries.get(type);
        int stacks = entry == null || gameTime - entry.lastHit() > DECAY_TICKS ? 1 : Math.min(MAX_STACKS, entry.stacks() + 1);
        entries.put(type, new Entry(stacks, gameTime));
    }

    private record Entry(int stacks, long lastHit) {
    }
}
