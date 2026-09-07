package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SteelFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void familyContainsBlockAndGrille() {
        assertEquals(2, DWMBlocks.STEEL_FAMILY.size());
        assertTrue(DWMBlocks.STEEL_FAMILY.contains(DWMBlocks.STEEL_BLOCK));
        assertTrue(DWMBlocks.STEEL_FAMILY.contains(DWMBlocks.STEEL_GRILLE));
        assertEquals(id("steel_block"), BuiltInRegistries.BLOCK.getKey(DWMBlocks.STEEL_BLOCK));
        assertEquals(id("steel_grille"), BuiltInRegistries.BLOCK.getKey(DWMBlocks.STEEL_GRILLE));
        assertEquals(id("steel_ingot"), BuiltInRegistries.ITEM.getKey(DWMItems.STEEL_INGOT));
    }

    @Test
    void grilleHasFullCubeCollision() {
        assertEquals(
                Shapes.block(),
                DWMBlocks.STEEL_GRILLE.defaultBlockState().getCollisionShape(
                        net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
                        net.minecraft.core.BlockPos.ZERO
                )
        );
        assertTrue(DWMBlocks.STEEL_GRILLE.defaultBlockState().requiresCorrectToolForDrops());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }
}
