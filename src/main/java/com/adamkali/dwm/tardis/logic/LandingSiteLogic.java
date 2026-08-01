package com.adamkali.dwm.tardis.logic;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Landing-site helpers for exterior relocation into a selected biome.
 */
public final class LandingSiteLogic {
    /** Default horizontal search radius for {@link ServerWorld#locateBiome}. */
    public static final int DEFAULT_SEARCH_RADIUS = 6400;

    /** Sample interval passed to {@link ServerWorld#locateBiome}. */
    public static final int LOCATE_INTERVAL = 64;

    private LandingSiteLogic() {
    }

    /**
     * Parses a biome registry id string into a {@link RegistryKey}.
     */
    public static Optional<RegistryKey<Biome>> parseBiome(@Nullable String biomeId) {
        if (biomeId == null || biomeId.isBlank()) {
            return Optional.empty();
        }
        Identifier id = Identifier.tryParse(biomeId);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(RegistryKey.of(RegistryKeys.BIOME, id));
    }

    /**
     * Locates a surface landing position in {@code biome} near {@code searchOrigin}.
     * Empty when the biome cannot be found or no valid surface cell exists.
     */
    public static Optional<BlockPos> findLanding(
            ServerWorld world,
            RegistryKey<Biome> biome,
            BlockPos searchOrigin,
            int radius
    ) {
        if (world == null || biome == null || searchOrigin == null) {
            return Optional.empty();
        }
        Pair<BlockPos, RegistryEntry<Biome>> located = world.locateBiome(
                entry -> entry.matchesKey(biome),
                searchOrigin,
                Math.max(1, radius),
                LOCATE_INTERVAL,
                LOCATE_INTERVAL
        );
        if (located == null) {
            return Optional.empty();
        }
        BlockPos biomePos = located.getFirst();
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, biomePos.getX(), biomePos.getZ());
        BlockPos landing = new BlockPos(biomePos.getX(), topY, biomePos.getZ());
        if (!isValidLanding(world, landing)) {
            return Optional.empty();
        }
        return Optional.of(landing);
    }

    public static Optional<BlockPos> findLanding(
            ServerWorld world,
            RegistryKey<Biome> biome,
            BlockPos searchOrigin
    ) {
        return findLanding(world, biome, searchOrigin, DEFAULT_SEARCH_RADIUS);
    }

    /**
     * Shell needs a solid floor under {@code pos} and replaceable space at {@code pos} and above.
     */
    public static boolean isValidLanding(WorldView world, BlockPos pos) {
        if (world == null || pos == null || world.isOutOfHeightLimit(pos)) {
            return false;
        }
        BlockState below = world.getBlockState(pos.down());
        if (!below.isSideSolidFullSquare(world, pos.down(), Direction.UP)) {
            return false;
        }
        BlockState feet = world.getBlockState(pos);
        BlockState head = world.getBlockState(pos.up());
        return (feet.isAir() || feet.isReplaceable()) && (head.isAir() || head.isReplaceable());
    }
}
