package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekaniumArchitectureFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void architectureContainsExactIdsAndSizes() {
        assertEquals(8, DWMBlocks.SILVER_DALEKANIUM_ARCHITECTURE.size());
        assertEquals(8, DWMBlocks.BRONZE_DALEKANIUM_ARCHITECTURE.size());
        assertEquals(16, DWMBlocks.DALEKANIUM_ARCHITECTURE.size());
        assertTrue(DWMBlocks.DALEKANIUM_ARCHITECTURE.containsAll(DWMBlocks.SILVER_DALEKANIUM_ARCHITECTURE));
        assertTrue(DWMBlocks.DALEKANIUM_ARCHITECTURE.containsAll(DWMBlocks.BRONZE_DALEKANIUM_ARCHITECTURE));

        assertEquals(id("silver_dalekanium_wall"), idOf(DWMBlocks.SILVER_DALEKANIUM_WALL));
        assertEquals(id("silver_dalekanium_riveted_wall"), idOf(DWMBlocks.SILVER_DALEKANIUM_RIVETED_WALL));
        assertEquals(id("silver_dalekanium_floor"), idOf(DWMBlocks.SILVER_DALEKANIUM_FLOOR));
        assertEquals(id("silver_dalekanium_panel"), idOf(DWMBlocks.SILVER_DALEKANIUM_PANEL));
        assertEquals(id("silver_dalekanium_light"), idOf(DWMBlocks.SILVER_DALEKANIUM_LIGHT));
        assertEquals(id("silver_dalekanium_door"), idOf(DWMBlocks.SILVER_DALEKANIUM_DOOR));
        assertEquals(id("silver_dalekanium_damaged_wall"), idOf(DWMBlocks.SILVER_DALEKANIUM_DAMAGED_WALL));
        assertEquals(id("silver_dalekanium_damaged_panel"), idOf(DWMBlocks.SILVER_DALEKANIUM_DAMAGED_PANEL));

        assertEquals(id("bronze_dalekanium_wall"), idOf(DWMBlocks.BRONZE_DALEKANIUM_WALL));
        assertEquals(id("bronze_dalekanium_door"), idOf(DWMBlocks.BRONZE_DALEKANIUM_DOOR));
    }

    @Test
    void noGlassOrGrilleDalekaniumIds() {
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("silver_dalekanium_glass")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("bronze_dalekanium_glass")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("dalekanium_glass")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("silver_dalekanium_grille")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("bronze_dalekanium_grille")));
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("dalekanium_grille")));
    }

    @Test
    void lightsEmitLevel15() {
        assertEquals(15, DWMBlocks.SILVER_DALEKANIUM_LIGHT.defaultBlockState().getLightEmission());
        assertEquals(15, DWMBlocks.BRONZE_DALEKANIUM_LIGHT.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.SILVER_DALEKANIUM_WALL.defaultBlockState().getLightEmission());
    }

    @Test
    void doorsAreIronLikeDoorBlocks() {
        assertInstanceOf(DoorBlock.class, DWMBlocks.SILVER_DALEKANIUM_DOOR);
        assertInstanceOf(DoorBlock.class, DWMBlocks.BRONZE_DALEKANIUM_DOOR);
        assertFalse(DWMWoodTypes.DALEKANIUM_SET.canOpenByHand());
        assertFalse(DWMBlocks.SILVER_DALEKANIUM_DOOR.defaultBlockState().getValue(DoorBlock.OPEN));
        assertFalse(DWMBlocks.SILVER_DALEKANIUM_DOOR.defaultBlockState().getValue(DoorBlock.POWERED));
    }

    @Test
    void architectureSolidsRequireCorrectTool() {
        for (Block block : DWMBlocks.DALEKANIUM_ARCHITECTURE) {
            if (block instanceof DoorBlock) {
                continue;
            }
            assertTrue(
                    block.defaultBlockState().requiresCorrectToolForDrops(),
                    () -> idOf(block) + " must require correct tool"
            );
        }
    }

    @Test
    void steelGrilleHasFullCollisionAndIsNotDalekaniumArchitecture() {
        assertEquals(id("steel_grille"), idOf(DWMBlocks.STEEL_GRILLE));
        assertEquals(
                Shapes.block(),
                DWMBlocks.STEEL_GRILLE.defaultBlockState().getCollisionShape(
                        net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
                        net.minecraft.core.BlockPos.ZERO
                )
        );
        assertFalse(DWMBlocks.DALEKANIUM_ARCHITECTURE.contains(DWMBlocks.STEEL_GRILLE));
        assertFalse(DWMBlocks.DALEKANIUM_ARCHITECTURE.contains(DWMBlocks.STEEL_BLOCK));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }

    private static Identifier idOf(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }
}
