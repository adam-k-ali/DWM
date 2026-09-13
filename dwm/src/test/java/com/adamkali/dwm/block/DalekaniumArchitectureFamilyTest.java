package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekaniumArchitectureFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void architectureContainsExactIdsAndSizes() {
        assertEquals(2, DWMBlocks.SILVER_DALEKANIUM_ARCHITECTURE.size());
        assertEquals(2, DWMBlocks.BRONZE_DALEKANIUM_ARCHITECTURE.size());
        assertEquals(4, DWMBlocks.DALEKANIUM_ARCHITECTURE.size());
        assertTrue(DWMBlocks.DALEKANIUM_ARCHITECTURE.containsAll(DWMBlocks.SILVER_DALEKANIUM_ARCHITECTURE));
        assertTrue(DWMBlocks.DALEKANIUM_ARCHITECTURE.containsAll(DWMBlocks.BRONZE_DALEKANIUM_ARCHITECTURE));

        assertEquals(id("silver_dalekanium_panel"), idOf(DWMBlocks.SILVER_DALEKANIUM_PANEL));
        assertEquals(id("silver_dalekanium_riveted_wall"), idOf(DWMBlocks.SILVER_DALEKANIUM_RIVETED_WALL));
        assertEquals(id("bronze_dalekanium_panel"), idOf(DWMBlocks.BRONZE_DALEKANIUM_PANEL));
        assertEquals(id("bronze_dalekanium_riveted_wall"), idOf(DWMBlocks.BRONZE_DALEKANIUM_RIVETED_WALL));
    }

    @Test
    void architectureSolidsAreMetalHardnessAndRequireTool() {
        for (Block block : DWMBlocks.DALEKANIUM_ARCHITECTURE) {
            assertEquals(4.0F, block.defaultDestroyTime(), () -> idOf(block) + " hardness");
            assertTrue(
                    block.defaultBlockState().requiresCorrectToolForDrops(),
                    () -> idOf(block) + " must require correct tool"
            );
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }

    private static Identifier idOf(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }
}
