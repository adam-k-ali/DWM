package com.adamkali.dwm.gametest;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

/**
 * Server-side perf regression for the BOTI/SOTO portal streams: how much work it takes to sample a whole stream
 * (what a viewer subscribing costs the server) and how many ticks that takes at the per-tick send budget.
 * Sampling is called directly (no networking) so the numbers are deterministic; wall-clock is only a generous ceiling.
 */
public class PortalStreamGameTests {
    /** Chunk columns sent per viewer per tick, mirrors {@code PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK}. */
    private static final int CHUNKS_PER_TICK = 8;
    private static final int MAX_STREAM_CHUNKS = 81;
    private static final long FULL_STREAM_CEILING_MS = 5_000L;

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 120)
    public void botiStream_FullSampleStaysWithinBudget(GameTestHelper context) {
        ServerLevel interior = context.getLevel();
        UUID tardisId = UUID.randomUUID();
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        FirstDoctorConsoleRoomPlacer.place(interior, origin, tardisId);

        context.runAfterDelay(40, () -> {
            int[] bounds = BotiInteriorSampler.streamChunkBounds(interior, origin);
            long start = System.nanoTime();
            int columns = 0;
            int sampled = 0;
            int blocks = 0;
            for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
                for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                    columns++;
                    var sample = BotiInteriorSampler.sampleStreamChunk(interior, tardisId, cx, cz);
                    if (sample != null) {
                        sampled++;
                        blocks += sample.blocks().size();
                    }
                }
            }
            reportAndAssert("BOTI", columns, sampled, blocks, System.nanoTime() - start);
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 120)
    public void sotoStream_FullSampleStaysWithinBudget(GameTestHelper context) {
        ServerLevel world = context.getLevel();
        BlockPos anchor = context.absolutePos(new BlockPos(1, 2, 1));
        world.setBlock(anchor.below(), Blocks.STONE.defaultBlockState(), 3);

        context.runAfterDelay(20, () -> {
            int[] bounds = SotoExteriorSampler.streamChunkBounds(world, anchor);
            long start = System.nanoTime();
            int columns = 0;
            int sampled = 0;
            int blocks = 0;
            for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
                for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                    columns++;
                    var sample = SotoExteriorSampler.sampleStreamChunk(world, anchor, cx, cz);
                    if (sample != null) {
                        sampled++;
                        blocks += sample.blocks().size();
                    }
                }
            }
            reportAndAssert("SOTO", columns, sampled, blocks, System.nanoTime() - start);
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void streamRadius_StaysWithinChunkBudget(GameTestHelper context) {
        int radius = PortalSampler.streamRadiusChunks(context.getLevel());
        int columns = (2 * radius + 1) * (2 * radius + 1);
        if (columns > MAX_STREAM_CHUNKS) {
            throw new AssertionError("Stream radius " + radius + " covers " + columns
                    + " chunk columns, above the " + MAX_STREAM_CHUNKS + " budget");
        }
        context.succeed();
    }

    private static void reportAndAssert(String kind, int columns, int sampled, int blocks, long nanos) {
        long ms = nanos / 1_000_000L;
        int ticksAtBudget = (columns + CHUNKS_PER_TICK - 1) / CHUNKS_PER_TICK;
        System.out.println("[portal-perf] " + kind + " stream: columns=" + columns + " sampled=" + sampled
                + " blocks=" + blocks + " fullSampleMs=" + ms + " ticksAtSendBudget=" + ticksAtBudget);
        if (columns > MAX_STREAM_CHUNKS) {
            throw new AssertionError(kind + " stream covers " + columns + " chunk columns (> " + MAX_STREAM_CHUNKS + ")");
        }
        if (sampled == 0) {
            throw new AssertionError(kind + " stream produced no samples; the footprint never became FULL");
        }
        if (ms > FULL_STREAM_CEILING_MS) {
            throw new AssertionError(kind + " full stream sample took " + ms + "ms (ceiling " + FULL_STREAM_CEILING_MS + "ms)");
        }
    }
}
