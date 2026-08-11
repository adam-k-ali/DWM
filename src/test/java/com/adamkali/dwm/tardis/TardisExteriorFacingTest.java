package com.adamkali.dwm.tardis;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TardisExteriorFacingTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void doorDirection_oppositeOfRawSkullSouthConvention() {
        assertEquals(Direction.NORTH, TardisExteriorFacing.doorDirection(0));
        assertEquals(Direction.EAST, TardisExteriorFacing.doorDirection(4));
        assertEquals(Direction.SOUTH, TardisExteriorFacing.doorDirection(8));
        assertEquals(Direction.WEST, TardisExteriorFacing.doorDirection(12));
    }
}
