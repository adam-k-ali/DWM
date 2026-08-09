package com.adamkali.dwm.block.wood;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TallDoorBlockTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void cardinalDoorIsTallDoorBlock() {
        assertInstanceOf(TallDoorBlock.class, DWMBlocks.CARDINAL_DOOR);
        assertTrue(DWMBlocks.CARDINAL.has(WoodFamilyFeature.TALL_DOOR));
        assertFalse(DWMBlocks.CARDINAL.has(WoodFamilyFeature.DOOR));
    }

    @Test
    void cellAndOriginHelpersRoundTrip() {
        BlockPos origin = new BlockPos(10, 64, 20);
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            BlockPos cell = TallDoorBlock.cellPos(origin, segment);
            assertEquals(origin.getY() + segment.index(), cell.getY());
            BlockState state = DWMBlocks.CARDINAL_DOOR.getDefaultState()
                    .with(TallDoorBlock.SEGMENT, segment)
                    .with(TallDoorBlock.FACING, Direction.NORTH);
            assertEquals(origin, TallDoorBlock.originPos(cell, state));
        }
    }

    @Test
    void shouldApplyPowerEdgeOnlyOnChange() {
        assertTrue(TallDoorBlock.shouldApplyPowerEdge(true, false));
        assertTrue(TallDoorBlock.shouldApplyPowerEdge(false, true));
        assertFalse(TallDoorBlock.shouldApplyPowerEdge(true, true));
        assertFalse(TallDoorBlock.shouldApplyPowerEdge(false, false));
    }

    @Test
    void isOriginOnlyBottomSegment() {
        BlockState bottom = DWMBlocks.CARDINAL_DOOR.getDefaultState()
                .with(TallDoorBlock.SEGMENT, TallDoorSegment.BOTTOM);
        BlockState middle = bottom.with(TallDoorBlock.SEGMENT, TallDoorSegment.MIDDLE);
        assertTrue(TallDoorBlock.isOrigin(bottom));
        assertFalse(TallDoorBlock.isOrigin(middle));
    }
}
