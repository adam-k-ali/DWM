package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkaroNaturalBlockFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void familyContainsExactPetrifiedIds() {
        assertEquals(8, DWMBlocks.PETRIFIED_FAMILY.size());
        assertEquals(2, DWMBlocks.PETRIFIED_NATURAL_BLOCKS.size());
        assertEquals(6, DWMBlocks.PETRIFIED_BUILDING_BLOCKS.size());
        assertTrue(DWMBlocks.PETRIFIED_FAMILY.containsAll(DWMBlocks.PETRIFIED_NATURAL_BLOCKS));
        assertTrue(DWMBlocks.PETRIFIED_FAMILY.containsAll(DWMBlocks.PETRIFIED_BUILDING_BLOCKS));

        assertEquals(id("petrified_log"), idOf(DWMBlocks.PETRIFIED_LOG));
        assertEquals(id("petrified_wood"), idOf(DWMBlocks.PETRIFIED_WOOD));
        assertEquals(id("stripped_petrified_log"), idOf(DWMBlocks.STRIPPED_PETRIFIED_LOG));
        assertEquals(id("stripped_petrified_wood"), idOf(DWMBlocks.STRIPPED_PETRIFIED_WOOD));
        assertEquals(id("petrified_planks"), idOf(DWMBlocks.PETRIFIED_PLANKS));
        assertEquals(id("petrified_stairs"), idOf(DWMBlocks.PETRIFIED_STAIRS));
        assertEquals(id("petrified_slab"), idOf(DWMBlocks.PETRIFIED_SLAB));
        assertEquals(id("petrified_wall"), idOf(DWMBlocks.PETRIFIED_WALL));
    }

    @Test
    void creativeTabsDoNotDuplicateNaturalAndBuilding() {
        for (Block natural : DWMBlocks.PETRIFIED_NATURAL_BLOCKS) {
            assertFalse(
                    DWMBlocks.PETRIFIED_BUILDING_BLOCKS.contains(natural),
                    () -> idOf(natural) + " must not appear in building subset"
            );
        }
        for (Block building : DWMBlocks.PETRIFIED_BUILDING_BLOCKS) {
            assertFalse(
                    DWMBlocks.PETRIFIED_NATURAL_BLOCKS.contains(building),
                    () -> idOf(building) + " must not appear in natural subset"
            );
        }
        assertTrue(DWMBlocks.PETRIFIED_NATURAL_BLOCKS.contains(DWMBlocks.PETRIFIED_LOG));
        assertTrue(DWMBlocks.PETRIFIED_NATURAL_BLOCKS.contains(DWMBlocks.PETRIFIED_WOOD));
        assertTrue(DWMBlocks.PETRIFIED_BUILDING_BLOCKS.contains(DWMBlocks.PETRIFIED_PLANKS));
        assertTrue(DWMBlocks.PETRIFIED_BUILDING_BLOCKS.contains(DWMBlocks.PETRIFIED_WALL));
    }

    @Test
    void petrifiedLogsAreAxisPillars() {
        assertInstanceOf(RotatedPillarBlock.class, DWMBlocks.PETRIFIED_LOG);
        assertInstanceOf(RotatedPillarBlock.class, DWMBlocks.PETRIFIED_WOOD);
        assertInstanceOf(RotatedPillarBlock.class, DWMBlocks.STRIPPED_PETRIFIED_LOG);
        assertInstanceOf(RotatedPillarBlock.class, DWMBlocks.STRIPPED_PETRIFIED_WOOD);
    }

    @Test
    void petrifiedRequiresPickaxeAndIsNonflammable() {
        FlammableBlockRegistry flammables = FlammableBlockRegistry.getDefaultInstance();
        for (Block block : DWMBlocks.PETRIFIED_FAMILY) {
            var state = block.defaultBlockState();
            assertTrue(
                    state.requiresCorrectToolForDrops(),
                    () -> idOf(block) + " must require correct tool for drops"
            );
            assertEquals(
                    0,
                    flammables.get(block).getIgniteOdds(),
                    () -> idOf(block) + " must not ignite"
            );
            assertEquals(
                    0,
                    flammables.get(block).getBurnOdds(),
                    () -> idOf(block) + " must not burn"
            );
            assertFalse(
                    state.ignitedByLava(),
                    () -> idOf(block) + " must not ignite by lava"
            );
        }
    }

    @Test
    void noCustomSkaroTerrainBlocksRegistered() {
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("skaro_stone")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("skaro_sand")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("skaro_sandstone")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("skaro_dirt")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("skaro_coarse_dirt")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("skaro_dust")));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }

    private static Identifier idOf(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }
}
