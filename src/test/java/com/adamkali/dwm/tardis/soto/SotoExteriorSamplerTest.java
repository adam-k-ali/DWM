package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SotoExteriorSamplerTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void isSotoVisible_excludesAirLightAndTardisBlock() {
        assertFalse(SotoExteriorSampler.isSotoVisible(Blocks.AIR.getDefaultState()));
        assertFalse(SotoExteriorSampler.isSotoVisible(Blocks.LIGHT.getDefaultState()));
        assertFalse(SotoExteriorSampler.isSotoVisible(DWMBlocks.TARDIS_BLOCK.getDefaultState()));
        assertTrue(SotoExteriorSampler.isSotoVisible(Blocks.STONE.getDefaultState()));
        assertTrue(SotoExteriorSampler.isSotoVisible(Blocks.CHEST.getDefaultState()));
    }

    @Test
    void filterVisible_excludesTardisBlock() {
        Map<BlockPos, BlockState> input = new HashMap<>();
        input.put(new BlockPos(0, 0, 0), Blocks.GRASS_BLOCK.getDefaultState());
        input.put(new BlockPos(1, 0, 0), Blocks.AIR.getDefaultState());
        input.put(new BlockPos(2, 0, 0), DWMBlocks.TARDIS_BLOCK.getDefaultState());
        input.put(new BlockPos(3, 0, 0), Blocks.LIGHT.getDefaultState());

        Map<BlockPos, BlockState> visible = SotoExteriorSampler.filterVisible(input);

        assertEquals(1, visible.size());
        assertTrue(visible.containsKey(new BlockPos(0, 0, 0)));
    }

    @Test
    void footprintOrigin_centersOnExteriorBlock() {
        BlockPos exterior = new BlockPos(100, 64, -20);
        BlockPos origin = SotoExteriorSampler.footprintOrigin(exterior);

        assertEquals(new BlockPos(95, 63, -25), origin);
        assertEquals(origin.add(SotoExteriorSampler.RELATIVE_TARDIS_POS), exterior);
    }

    @Test
    void isInsideFootprint_respectsBounds() {
        BlockPos exterior = new BlockPos(10, 70, 10);
        BlockPos origin = SotoExteriorSampler.footprintOrigin(exterior);

        assertTrue(SotoExteriorSampler.isInsideFootprint(origin, origin));
        assertTrue(SotoExteriorSampler.isInsideFootprint(exterior, origin));
        assertTrue(SotoExteriorSampler.isInsideFootprint(
                origin.add(SotoExteriorSampler.SIZE_X - 1, SotoExteriorSampler.SIZE_Y - 1, SotoExteriorSampler.SIZE_Z - 1),
                origin));
        assertFalse(SotoExteriorSampler.isInsideFootprint(origin.add(SotoExteriorSampler.SIZE_X, 0, 0), origin));
        assertFalse(SotoExteriorSampler.isInsideFootprint(origin.add(0, -1, 0), origin));
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
}
