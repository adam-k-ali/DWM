package com.adamkali.dwm.entity;

import com.adamkali.dwm.world.SkaroDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Named patrol spawn constants and server-side targeting/spawn helpers.
 * Combat, model, and sounds remain owned by the core Dalek entity.
 */
public final class DalekPatrolLogic {
    public static final int STANDARD_SPAWN_WEIGHT = 100;
    public static final int SPARSE_SPAWN_WEIGHT = 25;
    public static final int STANDARD_MIN_COUNT = 1;
    public static final int STANDARD_MAX_COUNT = 3;
    public static final int SPARSE_MIN_COUNT = 1;
    public static final int SPARSE_MAX_COUNT = 2;
    public static final double STANDARD_SPAWN_CHARGE = 0.7;
    public static final double STANDARD_SPAWN_ENERGY_BUDGET = 0.15;
    public static final double SPARSE_SPAWN_CHARGE = 1.0;
    public static final double SPARSE_SPAWN_ENERGY_BUDGET = 0.12;
    public static final double TARGET_SHARE_RADIUS = 16.0;

    private DalekPatrolLogic() {
    }

    public static boolean isWorldPopulationReason(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
    }

    public static boolean allowsWorldPopulation(EntitySpawnReason reason, boolean isSkaro) {
        if (!isWorldPopulationReason(reason)) {
            return true;
        }
        return isSkaro;
    }

    public static boolean canSpawn(
            EntityType<? extends Mob> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (!Mob.checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        return allowsWorldPopulation(reason, SkaroDimensions.isSkaroWorld(level.getLevel()));
    }

    public static boolean shouldShareTarget(
            boolean selfIdle,
            boolean sameDimension,
            double distanceSquared,
            boolean allyHasLivingPlayerTarget
    ) {
        if (!selfIdle || !sameDimension || !allyHasLivingPlayerTarget) {
            return false;
        }
        return distanceSquared <= TARGET_SHARE_RADIUS * TARGET_SHARE_RADIUS;
    }
}
