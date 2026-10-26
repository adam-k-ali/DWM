package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Spawn policy for {@link ThalEntity}. Thals never spawn naturally from biome spawn lists;
 * residents are placed by settlements. {@link #allowsNaturalSpawn} is the pure decision so it can be
 * unit-tested without a world.
 */
public final class ThalSpawnRules {
    private ThalSpawnRules() {
    }

    /**
     * Whether the spawn {@code reason} is acceptable. Ambient reasons
     * ({@link EntitySpawnReason#NATURAL}, {@link EntitySpawnReason#CHUNK_GENERATION}) are only allowed
     * inside a settlement; every other reason (spawn egg, command, structure, triggered, ...) is allowed.
     */
    public static boolean allowsNaturalSpawn(EntitySpawnReason reason, boolean insideSettlement) {
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            return insideSettlement;
        }
        return true;
    }

    /**
     * Whether {@code pos} lies inside a Thal settlement.
     *
     * <p>Hook for the plateau settlement: look up the settlement structure at {@code pos}
     * (for example {@code ServerLevel#structureManager().getStructureWithPieceAt(pos, key)}).
     */
    public static boolean isInsideSettlement(ServerLevelAccessor level, BlockPos pos) {
        // settlement structure check added with the plateau settlement
        return false;
    }

    /** {@link net.minecraft.world.entity.SpawnPlacements.SpawnPredicate} used by {@link DWMEntityTypes#THAL}. */
    public static boolean checkSpawnRules(
            EntityType<? extends ThalEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        return Mob.checkMobSpawnRules(type, level, reason, pos, random)
                && allowsNaturalSpawn(reason, isInsideSettlement(level, pos));
    }
}
