package com.adamkali.dwm.tardis.interior;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisInteriorDoorRenderAnchorTest {

    @Test
    void loneDoor_isPrimary() {
        Set<BlockPos> bank = Set.of(new BlockPos(0, 1, 0));
        assertTrue(TardisInteriorDoorRenderAnchor.isPrimary(
                new BlockPos(0, 1, 0), Direction.SOUTH, bank::contains));
    }

    @Test
    void southFacingBank_onlyBottomWestIsPrimary() {
        Set<BlockPos> bank = new HashSet<>();
        for (int y = 1; y <= 2; y++) {
            for (int x = 4; x <= 6; x++) {
                bank.add(new BlockPos(x, y, 0));
            }
        }

        BlockPos primary = null;
        int primaryCount = 0;
        for (BlockPos pos : bank) {
            if (TardisInteriorDoorRenderAnchor.isPrimary(pos, Direction.SOUTH, bank::contains)) {
                primaryCount++;
                primary = pos;
            }
        }

        assertEquals(1, primaryCount);
        assertEquals(new BlockPos(4, 1, 0), primary);
        assertFalse(TardisInteriorDoorRenderAnchor.isPrimary(
                new BlockPos(5, 1, 0), Direction.SOUTH, bank::contains));
        assertFalse(TardisInteriorDoorRenderAnchor.isPrimary(
                new BlockPos(4, 2, 0), Direction.SOUTH, bank::contains));
    }

    @Test
    void eastFacingBank_onlyBottomNorthIsPrimary() {
        // Facing east: rotateYClockwise = south → primary has no door to the south (max Z).
        Set<BlockPos> bank = new HashSet<>();
        for (int y = 1; y <= 2; y++) {
            for (int z = 3; z <= 5; z++) {
                bank.add(new BlockPos(0, y, z));
            }
        }

        BlockPos primary = null;
        int primaryCount = 0;
        for (BlockPos pos : bank) {
            if (TardisInteriorDoorRenderAnchor.isPrimary(pos, Direction.EAST, bank::contains)) {
                primaryCount++;
                primary = pos;
            }
        }

        assertEquals(1, primaryCount);
        assertEquals(new BlockPos(0, 1, 5), primary);
    }
}
