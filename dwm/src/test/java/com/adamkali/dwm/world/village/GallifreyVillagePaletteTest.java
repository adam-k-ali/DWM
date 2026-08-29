package com.adamkali.dwm.world.village;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class GallifreyVillagePaletteTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void oakStairsKeepFacingWhenMappedToAsh() {
        BlockState oak = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState replaced = GallifreyVillagePalette.replace(oak);
        assertEquals(DWMBlocks.ASH_STAIRS, replaced.getBlock());
        assertEquals(Direction.EAST, replaced.getValue(StairBlock.FACING));
    }

    @Test
    void unmappedBlocksAreUnchanged() {
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        assertSame(pane, GallifreyVillagePalette.replace(pane));
    }

    @Test
    void oakDoorMapsToAshNotCardinal() {
        assertFalse(GallifreyVillagePalette.usesCardinalDoor());
        assertEquals(DWMBlocks.ASH_DOOR, GallifreyVillagePalette.replace(Blocks.OAK_DOOR.defaultBlockState()).getBlock());
        assertFalse(GallifreyVillagePalette.mapsTo(Blocks.OAK_DOOR, DWMBlocks.CARDINAL_DOOR));
    }
}
