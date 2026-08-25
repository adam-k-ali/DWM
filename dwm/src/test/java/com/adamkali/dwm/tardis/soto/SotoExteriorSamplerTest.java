package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;

class SotoExteriorSamplerTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void isSotoVisible_excludesAirLightAndTardisBlock() {
        assertFalse(SotoExteriorSampler.isSotoVisible(Blocks.AIR.defaultBlockState()));
        assertFalse(SotoExteriorSampler.isSotoVisible(Blocks.LIGHT.defaultBlockState()));
        assertFalse(SotoExteriorSampler.isSotoVisible(DWMBlocks.TARDIS_BLOCK.defaultBlockState()));
        assertTrue(SotoExteriorSampler.isSotoVisible(Blocks.STONE.defaultBlockState()));
        assertTrue(SotoExteriorSampler.isSotoVisible(Blocks.CHEST.defaultBlockState()));
    }

    @Test
    void filterVisible_excludesTardisBlock() {
        Map<BlockPos, BlockState> input = new HashMap<>();
        input.put(new BlockPos(0, 0, 0), Blocks.GRASS_BLOCK.defaultBlockState());
        input.put(new BlockPos(1, 0, 0), Blocks.AIR.defaultBlockState());
        input.put(new BlockPos(2, 0, 0), DWMBlocks.TARDIS_BLOCK.defaultBlockState());
        input.put(new BlockPos(3, 0, 0), Blocks.LIGHT.defaultBlockState());

        Map<BlockPos, BlockState> visible = SotoExteriorSampler.filterVisible(input);

        assertEquals(1, visible.size());
        assertTrue(visible.containsKey(new BlockPos(0, 0, 0)));
    }

    @Test
    void footprintOrigin_centersOnExteriorBlock() {
        BlockPos exterior = new BlockPos(100, 64, -20);
        BlockPos origin = SotoExteriorSampler.footprintOrigin(exterior);

        assertEquals(new BlockPos(95, 63, -25), origin);
        assertEquals(origin.offset(SotoExteriorSampler.RELATIVE_TARDIS_POS), exterior);
    }

    @Test
    void isInsideFootprint_respectsBounds() {
        BlockPos exterior = new BlockPos(10, 70, 10);
        BlockPos origin = SotoExteriorSampler.footprintOrigin(exterior);

        assertTrue(SotoExteriorSampler.isInsideFootprint(origin, origin));
        assertTrue(SotoExteriorSampler.isInsideFootprint(exterior, origin));
        assertTrue(SotoExteriorSampler.isInsideFootprint(
                origin.offset(SotoExteriorSampler.SIZE_X - 1, SotoExteriorSampler.SIZE_Y - 1, SotoExteriorSampler.SIZE_Z - 1),
                origin));
        assertFalse(SotoExteriorSampler.isInsideFootprint(origin.offset(SotoExteriorSampler.SIZE_X, 0, 0), origin));
        assertFalse(SotoExteriorSampler.isInsideFootprint(origin.offset(0, -1, 0), origin));
    }

    @Test
    void relativePositions_fitBotiPosCodec() {
        for (int x = 0; x < SotoExteriorSampler.SIZE_X; x++) {
            for (int y = 0; y < SotoExteriorSampler.SIZE_Y; y++) {
                for (int z = 0; z < SotoExteriorSampler.SIZE_Z; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    assertEquals(pos, BotiRelativePosCodec.unpack(BotiRelativePosCodec.pack(pos)));
                }
            }
        }
    }

    @Test
    void streamChunkBounds_usesConfiguredRadius() {
        BlockPos exterior = new BlockPos(100, 64, -20);
        int[] bounds = SotoExteriorSampler.streamChunkBounds(exterior);
        int cx = 100 >> 4;
        int cz = -20 >> 4;
        assertEquals(cx - SotoExteriorSampler.STREAM_RADIUS_CHUNKS, bounds[0]);
        assertEquals(cx + SotoExteriorSampler.STREAM_RADIUS_CHUNKS, bounds[1]);
        assertEquals(cz - SotoExteriorSampler.STREAM_RADIUS_CHUNKS, bounds[2]);
        assertEquals(cz + SotoExteriorSampler.STREAM_RADIUS_CHUNKS, bounds[3]);
    }

    @Test
    void isInsideStreamRadius_respectsChebyshevAndY() {
        BlockPos exterior = new BlockPos(16, 70, 16);
        assertTrue(SotoExteriorSampler.isInsideStreamRadius(exterior, exterior));
        assertTrue(SotoExteriorSampler.isInsideStreamRadius(exterior.offset(32, 0, 0), exterior));
        assertFalse(SotoExteriorSampler.isInsideStreamRadius(exterior.offset(48, 0, 0), exterior));
        assertFalse(SotoExteriorSampler.isInsideStreamRadius(
                exterior.offset(0, SotoExteriorSampler.STREAM_Y_RADIUS + 1, 0), exterior));
    }

    @Test
    void streamBox_coversRadius() {
        BlockPos exterior = new BlockPos(0, 64, 0);
        var box = SotoExteriorSampler.streamBox(exterior);
        int half = SotoExteriorSampler.STREAM_RADIUS_CHUNKS * 16;
        assertEquals(-half, box.minX, 1e-6);
        assertEquals(half + 1, box.maxX, 1e-6);
        assertEquals(64 - SotoExteriorSampler.STREAM_Y_RADIUS, box.minY, 1e-6);
    }
}
