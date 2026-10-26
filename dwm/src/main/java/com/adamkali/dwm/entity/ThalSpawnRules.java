package com.adamkali.dwm.entity;

import com.adamkali.dwm.world.structure.ThalSettlementStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.function.Predicate;

/**
 * Spawn policy for {@link ThalEntity}. Thals never spawn naturally from biome spawn lists; residents are
 * placed by {@link ThalSettlementStructure settlement} pieces, and only positions inside a settlement may
 * spawn naturally. {@link #allowsNaturalSpawn} is the pure decision so it can be unit-tested without a world.
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
     * Whether {@code pos} lies inside a Thal settlement: within the bounding box of any piece of a
     * {@code dwm:thal_settlement} structure. Only a live {@link ServerLevel} is queried: a worldgen region
     * would resolve structure references off the main thread, so it never counts as inside.
     */
    public static boolean isInsideSettlement(ServerLevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return serverLevel.structureManager()
                .getStructureWithPieceAt(pos, (Predicate<Holder<Structure>>) holder -> holder.is(ThalSettlementStructure.KEY))
                .isValid();
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
