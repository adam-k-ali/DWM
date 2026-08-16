package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Pure exterior-atmosphere instruments for Panel1 readers.
 * Samples the linked exterior cell, never the interior.
 */
public final class ExteriorEnvironmentReadout {
    public static final float NO_SIGNAL = Float.NaN;

    public enum DimensionKind {
        OVERWORLD,
        NETHER,
        END,
        OTHER
    }

    /**
     * Facts needed to compute a readout without a live world (unit tests).
     */
    public record Sample(
            boolean noSignal,
            DimensionKind dimension,
            int y,
            int seaLevel,
            boolean waterlogged,
            boolean hasAir,
            float biomeTemperature,
            boolean thundering
    ) {
        public static Sample none() {
            return new Sample(true, DimensionKind.OTHER, 0, 63, false, false, 0.0F, false);
        }
    }

    public record Reading(
            boolean noSignal,
            float oxygen,
            float pressure,
            float temperature,
            float radiation
    ) {
        public static Reading none() {
            return new Reading(true, NO_SIGNAL, NO_SIGNAL, NO_SIGNAL, NO_SIGNAL);
        }

        public float needle(float value) {
            if (noSignal || Float.isNaN(value)) {
                return 0.0F;
            }
            return Math.max(0.0F, Math.min(1.0F, value));
        }
    }

    private ExteriorEnvironmentReadout() {
    }

    public static boolean isNoSignal(float value) {
        return Float.isNaN(value);
    }

    public static Reading fromSample(Sample sample) {
        if (sample == null || sample.noSignal()) {
            return Reading.none();
        }
        return new Reading(
                false,
                oxygen(sample),
                pressure(sample),
                temperature(sample),
                radiation(sample)
        );
    }

    public static float oxygen(Sample sample) {
        if (sample.waterlogged() || !sample.hasAir()) {
            return 0.0F;
        }
        return switch (sample.dimension()) {
            case NETHER -> 0.35F;
            case END -> 0.45F;
            case OVERWORLD, OTHER -> 1.0F;
        };
    }

    public static float pressure(Sample sample) {
        float span = 128.0F;
        float relative = (sample.seaLevel() - sample.y()) / span;
        float baseline = switch (sample.dimension()) {
            case NETHER -> 0.7F;
            case END -> 0.35F;
            case OVERWORLD, OTHER -> 0.5F;
        };
        return clamp01(baseline + relative * 0.4F);
    }

    public static float temperature(Sample sample) {
        // Vanilla biome temperature is typically -0.5..2.0.
        return clamp01((sample.biomeTemperature() + 0.5F) / 2.5F);
    }

    public static float radiation(Sample sample) {
        return switch (sample.dimension()) {
            case NETHER -> 0.9F;
            case END -> 0.55F;
            case OVERWORLD, OTHER -> sample.thundering() ? 0.35F : 0.12F;
        };
    }

    /**
     * Samples the linked exterior. In flight or missing exterior → {@link Reading#none()}.
     */
    public static Reading sample(
            @Nullable ServerLevel exteriorWorld,
            @Nullable BlockPos exteriorPos,
            boolean inFlight
    ) {
        if (inFlight || exteriorWorld == null || exteriorPos == null) {
            return Reading.none();
        }
        return fromSample(sampleFacts(exteriorWorld, exteriorPos));
    }

    public static Sample sampleFacts(ServerLevel world, BlockPos exteriorPos) {
        BlockPos atmosphere = atmospherePos(world, exteriorPos);
        BlockState state = world.getBlockState(atmosphere);
        boolean waterlogged = isWaterlogged(state);
        boolean hasAir = !waterlogged && (state.isAir() || state.canBeReplaced());
        float biomeTemp = world.getBiome(exteriorPos).value().getBaseTemperature();
        return new Sample(
                false,
                kindOf(world),
                exteriorPos.getY(),
                world.getSeaLevel(),
                waterlogged,
                hasAir,
                biomeTemp,
                world.isThundering()
        );
    }

    public static DimensionKind kindOf(Level world) {
        if (world == null) {
            return DimensionKind.OTHER;
        }
        if (world.dimension() == Level.NETHER) {
            return DimensionKind.NETHER;
        }
        if (world.dimension() == Level.END) {
            return DimensionKind.END;
        }
        if (world.dimension() == Level.OVERWORLD) {
            return DimensionKind.OVERWORLD;
        }
        String id = world.dimension().identifier().toString();
        if (id.endsWith("the_nether")) {
            return DimensionKind.NETHER;
        }
        if (id.endsWith("the_end")) {
            return DimensionKind.END;
        }
        return DimensionKind.OTHER;
    }

    public static DimensionKind kindOf(@Nullable String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return DimensionKind.OTHER;
        }
        if (dimensionId.endsWith("the_nether")) {
            return DimensionKind.NETHER;
        }
        if (dimensionId.endsWith("the_end")) {
            return DimensionKind.END;
        }
        if (dimensionId.endsWith("overworld")) {
            return DimensionKind.OVERWORLD;
        }
        return DimensionKind.OTHER;
    }

    private static BlockPos atmospherePos(ServerLevel world, BlockPos exteriorPos) {
        BlockState shell = world.getBlockState(exteriorPos);
        if (shell.is(DWMBlocks.TARDIS_BLOCK)) {
            return exteriorPos.above();
        }
        return exteriorPos;
    }

    private static boolean isWaterlogged(BlockState state) {
        if (state.getFluidState().is(FluidTags.WATER)) {
            return true;
        }
        return state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
