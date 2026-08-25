package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.CoordinateLockLogic.Axis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateLockLogicTest {
    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void apply_pinsLockedAxesToCurrentExterior() {
        TardisDataModel model = new TardisDataModel();
        model.setExteriorLocation("minecraft:overworld", 10, 64, -3, 0);
        model.lockX = true;
        model.lockZ = true;

        BlockPos pinned = CoordinateLockLogic.apply(new BlockPos(100, 80, 200), model);

        assertEquals(new BlockPos(10, 80, -3), pinned);
    }

    @Test
    void apply_leavesResolvedWhenUnlockedOrMissingExterior() {
        TardisDataModel model = new TardisDataModel();
        BlockPos resolved = new BlockPos(5, 70, 9);
        assertEquals(resolved, CoordinateLockLogic.apply(resolved, model));

        model.setExteriorLocation("minecraft:overworld", 1, 2, 3, 0);
        assertEquals(resolved, CoordinateLockLogic.apply(resolved, model));
        assertNull(CoordinateLockLogic.apply(null, model));
    }

    @Test
    void toggle_flipsOneAxis() {
        TardisDataModel model = new TardisDataModel();
        assertFalse(CoordinateLockLogic.anyLocked(model));
        assertTrue(CoordinateLockLogic.toggle(model, Axis.Y));
        assertTrue(model.lockY);
        assertFalse(model.lockX);
        assertTrue(CoordinateLockLogic.isLocked(model, Axis.Y));
        assertFalse(CoordinateLockLogic.toggle(model, Axis.Y));
        assertFalse(model.lockY);
    }
}
