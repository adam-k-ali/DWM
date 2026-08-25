package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Stabilisers toggle and unstabilised landing scatter helpers.
 * When enabled (default), landings keep the resolved destination.
 * When disabled, materialise scatters horizontally then re-validates.
 */
public final class StabiliserLogic {
    public static final int SCATTER_RADIUS = 24;
    public static final int MIN_OFFSET = 4;
    public static final int MAX_SAMPLES = 12;

    private StabiliserLogic() {
    }

    public static boolean isEnabled(@Nullable TardisDataModel model) {
        // Null (older Gson saves) and true both mean precise landing.
        return model == null || model.stabilisersEnabled == null || model.stabilisersEnabled;
    }

    /**
     * Flips {@link TardisDataModel#stabilisersEnabled} and marks the model dirty.
     *
     * @return the new enabled state, or {@code true} when {@code model} is null
     */
    public static boolean toggle(@Nullable TardisDataModel model) {
        if (model == null) {
            return true;
        }
        boolean next = !isEnabled(model);
        model.stabilisersEnabled = next;
        model.setChanged();
        return next;
    }

    /**
     * When stabilisers are on, returns {@code center}. When off, scatters horizontally
     * within [{@link #MIN_OFFSET}, {@link #SCATTER_RADIUS}] and re-validates the landing.
     */
    public static Optional<BlockPos> applyScatter(
            @Nullable ServerLevel world,
            @Nullable BlockPos center,
            @Nullable Direction doorFacing,
            @Nullable TardisDataModel model,
            @Nullable RandomSource random
    ) {
        if (center == null) {
            return Optional.empty();
        }
        if (isEnabled(model)) {
            return Optional.of(center);
        }
        if (world == null || doorFacing == null || random == null) {
            return Optional.empty();
        }

        int lastX = center.getX();
        int lastZ = center.getZ();
        for (int i = 0; i < MAX_SAMPLES; i++) {
            int[] offset = sampleHorizontalOffset(random);
            int x = center.getX() + offset[0];
            int z = center.getZ() + offset[1];
            lastX = x;
            lastZ = z;
            world.getChunk(x >> 4, z >> 4);
            int topY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, topY, z);
            if (LandingSiteLogic.isValidLanding(world, candidate, doorFacing)) {
                return Optional.of(candidate);
            }
        }
        return LandingSiteLogic.findNearbyValidLanding(world, lastX, lastZ, doorFacing);
    }

    /**
     * Pure: samples a non-zero horizontal offset with Chebyshev distance in
     * [{@link #MIN_OFFSET}, {@link #SCATTER_RADIUS}].
     *
     * @return {@code int[]{dx, dz}}
     */
    public static int[] sampleHorizontalOffset(RandomSource random) {
        int dx;
        int dz;
        do {
            dx = random.nextInt(-SCATTER_RADIUS, SCATTER_RADIUS + 1);
            dz = random.nextInt(-SCATTER_RADIUS, SCATTER_RADIUS + 1);
        } while (Math.max(Math.abs(dx), Math.abs(dz)) < MIN_OFFSET);
        return new int[]{dx, dz};
    }
}
