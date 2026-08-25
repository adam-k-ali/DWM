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

    @Test
    void facingRotationForDoor_roundTripsCardinalDoors() {
        for (Direction door : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            int rotation = TardisExteriorFacing.facingRotationForDoor(door);
            assertEquals(door, TardisExteriorFacing.doorDirection(rotation), door.getSerializedName());
        }
    }

    @Test
    void facingRotationForDoor_nonHorizontalFallsBackToNorth() {
        int rotation = TardisExteriorFacing.facingRotationForDoor(Direction.UP);
        assertEquals(Direction.NORTH, TardisExteriorFacing.doorDirection(rotation));
    }
}
