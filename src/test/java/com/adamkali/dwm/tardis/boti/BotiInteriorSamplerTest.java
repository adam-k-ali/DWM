package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotiInteriorSamplerTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void tearDown() {
        BotiPlotIndex.clear();
    }

    @Test
    void filterVisible_excludesAirLightAndInteriorDoor() {
        Map<BlockPos, BlockState> input = new HashMap<>();
        input.put(new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.getDefaultState());
        input.put(new BlockPos(1, 0, 0), Blocks.AIR.getDefaultState());
        input.put(new BlockPos(2, 0, 0), Blocks.LIGHT.getDefaultState());
        input.put(new BlockPos(3, 0, 0), DWMBlocks.TARDIS_INTERIOR_DOOR.getDefaultState());

        Map<BlockPos, BlockState> visible = BotiInteriorSampler.filterVisible(input);

        assertEquals(1, visible.size());
        assertTrue(visible.containsKey(new BlockPos(0, 0, 0)));
    }

    @Test
    void filterVisible_matchesBlueprintHelper() {
        Map<BlockPos, BlockState> fromLayout = FirstDoctorConsoleRoomLayout.botiVisiblePlacements();
        Map<BlockPos, BlockState> fromSampler = BotiInteriorSampler.filterVisible(FirstDoctorConsoleRoomLayout.placements());
        assertEquals(fromLayout, fromSampler);
        assertFalse(fromLayout.isEmpty());
    }

    @Test
    void isInsideFootprint_respectsRoomBounds() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);

        assertTrue(BotiInteriorSampler.isInsideFootprint(origin, origin));
        assertTrue(BotiInteriorSampler.isInsideFootprint(
                origin.add(BotiInteriorSampler.SIZE_X - 1, BotiInteriorSampler.SIZE_Y - 1, BotiInteriorSampler.SIZE_Z - 1),
                origin));
        assertFalse(BotiInteriorSampler.isInsideFootprint(origin.add(BotiInteriorSampler.SIZE_X, 0, 0), origin));
        assertFalse(BotiInteriorSampler.isInsideFootprint(origin.add(0, -1, 0), origin));
    }
}
