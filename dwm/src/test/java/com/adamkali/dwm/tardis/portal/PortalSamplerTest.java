package com.adamkali.dwm.tardis.portal;

import com.adamkali.dwm.MinecraftTestBootstrap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalSamplerTest {
    private static PortalSampler SAMPLER;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        SAMPLER = new PortalSampler() {
            @Override
            public boolean isVisible(BlockState state) {
                return state != null && !state.isAir() && !state.is(Blocks.LIGHT);
            }
        };
    }

    @Test
    void filterVisible_keepsOnlyVisibleBlocks() {
        Map<BlockPos, BlockState> input = new HashMap<>();
        input.put(new BlockPos(0, 0, 0), Blocks.STONE.defaultBlockState());
        input.put(new BlockPos(1, 0, 0), Blocks.AIR.defaultBlockState());
        input.put(new BlockPos(2, 0, 0), Blocks.LIGHT.defaultBlockState());
        input.put(new BlockPos(3, 0, 0), Blocks.CHEST.defaultBlockState());

        Map<BlockPos, BlockState> visible = SAMPLER.filterVisibleBlocks(input);

        assertEquals(2, visible.size());
        assertTrue(visible.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(visible.containsKey(new BlockPos(3, 0, 0)));
    }

    @Test
    void simulationRadiusChunks_isAtMostStreamRadius() {
        assertEquals(
                PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS,
                PortalSampler.simulationRadiusChunks(null)
        );
        assertTrue(PortalSampler.simulationRadiusChunks(null) <= PortalSampler.streamRadiusChunks((ServerLevel) null));
    }

    @Test
    void sampleAtmosphere_nullInputsReturnDefault() {
        assertSame(PortalAtmosphere.DEFAULT, PortalSampler.sampleAtmosphere(null, BlockPos.ZERO));
        assertSame(PortalAtmosphere.DEFAULT, PortalSampler.sampleAtmosphere(null, null));
    }

    @Test
    void streamRadiusChunks_nullInputsUseDefault() {
        assertEquals(PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS, PortalSampler.streamRadiusChunks((ServerLevel) null));
        assertEquals(PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS, PortalSampler.streamRadiusChunks((ServerPlayer) null));
    }

    @Test
    void streamYRadiusBlocks_matchesChunkWidth() {
        assertEquals(0, PortalSampler.streamYRadiusBlocks(0));
        assertEquals(32, PortalSampler.streamYRadiusBlocks(2));
        assertEquals(160, PortalSampler.streamYRadiusBlocks(10));
        assertEquals(0, PortalSampler.streamYRadiusBlocks(-1));
    }

    @Test
    void streamChunkBounds_isCenteredChebyshev() {
        BlockPos anchor = new BlockPos(100, 64, -20);
        int[] bounds = PortalSampler.streamChunkBounds(anchor, 2);
        int cx = 100 >> 4;
        int cz = -20 >> 4;
        assertEquals(cx - 2, bounds[0]);
        assertEquals(cx + 2, bounds[1]);
        assertEquals(cz - 2, bounds[2]);
        assertEquals(cz + 2, bounds[3]);
    }

    @Test
    void clipChunkBounds_intersectsViewAndPlot() {
        int[] view = new int[]{-10, 10, -10, 10};
        int[] clipped = PortalSampler.clipChunkBounds(view, 0, 3, 0, 3);
        assertEquals(0, clipped[0]);
        assertEquals(3, clipped[1]);
        assertEquals(0, clipped[2]);
        assertEquals(3, clipped[3]);
    }

    @Test
    void isInsideStreamRadius_respectsChebyshevAndY() {
        BlockPos anchor = new BlockPos(16, 70, 16);
        int radius = 2;
        int yRadius = PortalSampler.streamYRadiusBlocks(radius);
        assertTrue(PortalSampler.isInsideStreamRadius(anchor, anchor, radius, yRadius));
        assertTrue(PortalSampler.isInsideStreamRadius(anchor.offset(32, 0, 0), anchor, radius, yRadius));
        assertFalse(PortalSampler.isInsideStreamRadius(anchor.offset(48, 0, 0), anchor, radius, yRadius));
        assertFalse(PortalSampler.isInsideStreamRadius(anchor.offset(0, yRadius + 1, 0), anchor, radius, yRadius));
    }

    @Test
    void streamBox_coversRadius() {
        BlockPos anchor = new BlockPos(0, 64, 0);
        int radius = 2;
        int yRadius = PortalSampler.streamYRadiusBlocks(radius);
        AABB box = PortalSampler.streamBox(anchor, radius, yRadius);
        int half = radius * 16;
        assertEquals(-half, box.minX, 1e-6);
        assertEquals(half + 1, box.maxX, 1e-6);
        assertEquals(64 - yRadius, box.minY, 1e-6);
        assertEquals(64 + yRadius + 1, box.maxY, 1e-6);
    }

    @Test
    void streamTicketChunk_isAnchorChunk() {
        BlockPos anchor = new BlockPos(100, 64, -20);
        ChunkPos ticket = PortalSampler.streamTicketChunk(anchor);
        assertEquals(100 >> 4, ticket.x());
        assertEquals(-20 >> 4, ticket.z());
    }

    @Test
    void fogDistances_scaleWithRadius() {
        assertEquals(19.2f, PortalSampler.fogStartBlocks(2), 1e-4f);
        assertEquals(32.0f, PortalSampler.fogEndBlocks(2), 1e-4f);
        assertEquals(96.0f, PortalSampler.fogStartBlocks(10), 1e-4f);
        assertEquals(160.0f, PortalSampler.fogEndBlocks(10), 1e-4f);
        assertTrue(PortalSampler.fogEndBlocks(0) > PortalSampler.fogStartBlocks(0));
    }
}
