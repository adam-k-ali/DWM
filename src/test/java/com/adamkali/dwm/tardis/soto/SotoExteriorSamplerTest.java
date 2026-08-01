package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    void clampRadiusChunks_capsAtEight() {
        assertEquals(1, SotoExteriorSampler.clampRadiusChunks(0));
        assertEquals(1, SotoExteriorSampler.clampRadiusChunks(-5));
        assertEquals(4, SotoExteriorSampler.clampRadiusChunks(4));
        assertEquals(8, SotoExteriorSampler.clampRadiusChunks(8));
        assertEquals(8, SotoExteriorSampler.clampRadiusChunks(32));
        assertEquals(128, SotoExteriorSampler.radiusBlocks(8));
        assertEquals(64, SotoExteriorSampler.radiusBlocks(4));
    }

    @Test
    void relativeCoords_areTardisCentered() {
        BlockPos exterior = new BlockPos(100, 64, -20);
        assertEquals(BlockPos.ORIGIN, SotoExteriorSampler.toRelative(exterior, exterior));
        assertEquals(new BlockPos(2, -1, 3), SotoExteriorSampler.toRelative(exterior.add(2, -1, 3), exterior));
        assertEquals(exterior.add(2, -1, 3), SotoExteriorSampler.toWorld(new BlockPos(2, -1, 3), exterior));
    }

    @Test
    void isInsideMaxFootprint_usesChebyshevCap() {
        BlockPos exterior = new BlockPos(10, 70, 10);

        assertTrue(SotoExteriorSampler.isInsideMaxFootprint(exterior, exterior));
        assertTrue(SotoExteriorSampler.isInsideMaxFootprint(exterior.add(20, 0, 0), exterior));
        assertTrue(SotoExteriorSampler.isInsideMaxFootprint(
                exterior.add(SotoExteriorSampler.MAX_RADIUS_BLOCKS, 0, 0), exterior));
        assertFalse(SotoExteriorSampler.isInsideMaxFootprint(
                exterior.add(SotoExteriorSampler.MAX_RADIUS_BLOCKS + 1, 0, 0), exterior));
    }

    @Test
    void collectVisible_includesBlocksInFrontOfDoor() {
        BlockPos exterior = new BlockPos(0, 64, 0);
        // Rotation 0 = south (+Z). Seed at (0,64,1) and (0,65,1).
        Map<BlockPos, BlockState> world = new HashMap<>();
        world.put(exterior, DWMBlocks.TARDIS_BLOCK.getDefaultState());
        world.put(new BlockPos(0, 64, 2), Blocks.STONE.getDefaultState());
        world.put(new BlockPos(1, 64, 1), Blocks.DIRT.getDefaultState());

        SotoExteriorSampler.VisibilitySample sample = SotoExteriorSampler.collectVisible(
                exterior, 1, 0, lookup(world)
        );

        assertTrue(sample.blocks().containsKey(new BlockPos(0, 0, 2)));
        assertTrue(sample.blocks().containsKey(new BlockPos(1, 0, 1)));
        assertFalse(sample.blocks().containsKey(BlockPos.ORIGIN));
    }

    @Test
    void collectVisible_wallOccludesBlocksBehind() {
        BlockPos exterior = new BlockPos(0, 64, 0);
        Map<BlockPos, BlockState> world = new HashMap<>();
        world.put(exterior, DWMBlocks.TARDIS_BLOCK.getDefaultState());
        // Solid wall immediately south of door seeds (z=1)
        world.put(new BlockPos(0, 64, 1), Blocks.STONE.getDefaultState());
        world.put(new BlockPos(0, 65, 1), Blocks.STONE.getDefaultState());
        // Behind the wall
        world.put(new BlockPos(0, 64, 3), Blocks.GOLD_BLOCK.getDefaultState());

        SotoExteriorSampler.VisibilitySample sample = SotoExteriorSampler.collectVisible(
                exterior, 1, 0, lookup(world)
        );

        assertTrue(sample.blocks().containsKey(new BlockPos(0, 0, 1)));
        assertTrue(sample.blocks().containsKey(new BlockPos(0, 1, 1)));
        assertFalse(sample.blocks().containsKey(new BlockPos(0, 0, 3)));
    }

    @Test
    void collectVisible_openingRevealsFartherTerrain() {
        BlockPos exterior = new BlockPos(0, 64, 0);
        Map<BlockPos, BlockState> world = new HashMap<>();
        world.put(exterior, DWMBlocks.TARDIS_BLOCK.getDefaultState());
        // Wall with a gap at door height z=1 y=64 (air seed passes), stone at y=65
        world.put(new BlockPos(0, 65, 1), Blocks.STONE.getDefaultState());
        world.put(new BlockPos(0, 64, 4), Blocks.DIAMOND_BLOCK.getDefaultState());

        SotoExteriorSampler.VisibilitySample sample = SotoExteriorSampler.collectVisible(
                exterior, 1, 0, lookup(world)
        );

        assertTrue(sample.blocks().containsKey(new BlockPos(0, 0, 4)));
        assertTrue(sample.blocks().containsKey(new BlockPos(0, 1, 1)));
    }

    @Test
    void collectVisible_respectsRadius() {
        BlockPos exterior = new BlockPos(0, 64, 0);
        Map<BlockPos, BlockState> world = new HashMap<>();
        // radiusChunks=1 → 16 blocks. Place stone at z=17 (outside).
        world.put(new BlockPos(0, 64, 10), Blocks.STONE.getDefaultState());
        world.put(new BlockPos(0, 64, 17), Blocks.EMERALD_BLOCK.getDefaultState());

        SotoExteriorSampler.VisibilitySample sample = SotoExteriorSampler.collectVisible(
                exterior, 1, 0, lookup(world)
        );

        assertTrue(sample.blocks().containsKey(new BlockPos(0, 0, 10)));
        assertFalse(sample.blocks().containsKey(new BlockPos(0, 0, 17)));
    }

    @Test
    void collectVisible_stopsAtFloodVisitBudget() {
        BlockPos exterior = new BlockPos(0, 64, 0);
        // Open air in every cell — without a budget this would visit the full cube.
        SotoExteriorSampler.VisibilitySample sample = SotoExteriorSampler.collectVisible(
                exterior, 8, 0, pos -> Blocks.AIR.getDefaultState()
        );

        assertTrue(sample.floodedRel().size() <= SotoExteriorSampler.MAX_FLOOD_VISITS);
        assertTrue(sample.floodedRel().size() > 0);
    }

    private static Function<BlockPos, BlockState> lookup(Map<BlockPos, BlockState> world) {
        return pos -> world.getOrDefault(pos.toImmutable(), Blocks.AIR.getDefaultState());
    }
}
