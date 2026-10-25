package com.adamkali.dwm.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * A destroyed Dalek casing sometimes releases the Kaled mutant inside. Server-side only; the
 * roll is a pure function so the chance is testable without a world.
 */
public final class DalekMutantEmergence {
    public static final float EMERGE_CHANCE = 0.25F;

    private DalekMutantEmergence() {
    }

    /** {@code roll} is uniform in {@code [0, 1)}. */
    public static boolean shouldEmerge(float roll) {
        return roll < EMERGE_CHANCE;
    }

    /**
     * Spawns a mutant where {@code casing} stood, turned the same way and as persistent as the
     * casing, and sets it on the player who destroyed the casing (unless that player is in creative
     * or spectator).
     */
    public static @Nullable DalekMutantEntity release(ServerLevel level, DalekEntity casing) {
        DalekMutantEntity mutant = DWMEntityTypes.DALEK_MUTANT.create(level, EntitySpawnReason.TRIGGERED);
        if (mutant == null) {
            return null;
        }
        mutant.snapTo(casing.getX(), casing.getY(), casing.getZ(), casing.getYRot(), 0.0F);
        mutant.setYHeadRot(casing.getYHeadRot());
        mutant.finalizeSpawn(level, level.getCurrentDifficultyAt(mutant.blockPosition()), EntitySpawnReason.TRIGGERED, null);
        if (casing.isPersistenceRequired()) {
            // As in vanilla mob conversion: a named or otherwise persistent casing keeps its mutant.
            mutant.setPersistenceRequired();
        }
        LivingEntity killer = casing.getLastHurtByMob();
        if (killer instanceof Player player && !player.isCreative() && !player.isSpectator() && player.isAlive()) {
            mutant.setTarget(player);
        }
        level.addFreshEntity(mutant);
        return mutant;
    }
}
