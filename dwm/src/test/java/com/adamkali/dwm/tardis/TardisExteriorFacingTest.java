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
    void enterYaw_walkingStraightInFacesAwayFromInteriorDoor() {
        for (int rotation = 0; rotation < 16; rotation += 4) {
            // Heading into the shell is opposite to the direction its door faces.
            float intoDoorYaw = Direction.getYRot(TardisExteriorFacing.doorDirection(rotation)) + 180.0f;
            float yaw = TardisExteriorFacing.enterYaw(intoDoorYaw, rotation);
            assertEquals(0.0f, Math.abs(yaw) % 360.0f, 0.01f, "rotation " + rotation);
        }
    }

    @Test
    void exitYaw_walkingStraightOutFacesAwayFromShell() {
        for (int rotation = 0; rotation < 16; rotation += 4) {
            // Interior door faces south, so walking out is heading north (180).
            float yaw = TardisExteriorFacing.exitYaw(180.0f, rotation);
            float expected = Direction.getYRot(TardisExteriorFacing.doorDirection(rotation));
            assertEquals(net.minecraft.util.Mth.wrapDegrees(expected), yaw, 0.01f, "rotation " + rotation);
        }
    }

    @Test
    void enterThenExit_roundTripsYaw() {
        for (int rotation = 0; rotation < 16; rotation++) {
            float entered = TardisExteriorFacing.enterYaw(45.0f, rotation);
            assertEquals(45.0f, TardisExteriorFacing.exitYaw(entered, rotation), 0.01f, "rotation " + rotation);
        }
    }

    @Test
    void facingRotationForDoor_nonHorizontalFallsBackToNorth() {
        int rotation = TardisExteriorFacing.facingRotationForDoor(Direction.UP);
        assertEquals(Direction.NORTH, TardisExteriorFacing.doorDirection(rotation));
    }
}
