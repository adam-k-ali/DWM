package com.adamkali.dwm.entity;

import com.adamkali.dwm.block.TardisDecorShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TardisSeatPosesTest {
    private static final double EPSILON = 1e-6;

    @Test
    void seatHeights_matchCushionTops() {
        assertEquals(7.0 / 16.0, TardisDecorShapes.SMALL_CHAIR_SEAT_Y, EPSILON);
        assertEquals(9.1 / 16.0, TardisDecorShapes.LARGE_CHAIR_SEAT_Y, EPSILON);
    }

    @Test
    void seatPose_centersOnBlockAtSeatHeightWithFacingYaw() {
        BlockPos pos = new BlockPos(3, 64, -2);
        TardisSeatPoses.SeatPose pose =
                TardisSeatPoses.seatPose(pos, Direction.EAST, TardisDecorShapes.SMALL_CHAIR_SEAT_Y);

        assertEquals(3.5, pose.position().x, EPSILON);
        assertEquals(64.0 + TardisDecorShapes.SMALL_CHAIR_SEAT_Y, pose.position().y, EPSILON);
        assertEquals(-1.5, pose.position().z, EPSILON);
        assertEquals(Direction.getYRot(Direction.EAST), pose.yaw(), EPSILON);
    }

    @Test
    void seatPose_largeChairUsesHigherCushion() {
        Vec3 small = TardisSeatPoses.seatPose(BlockPos.ZERO, Direction.NORTH, TardisDecorShapes.SMALL_CHAIR_SEAT_Y)
                .position();
        Vec3 large = TardisSeatPoses.seatPose(BlockPos.ZERO, Direction.NORTH, TardisDecorShapes.LARGE_CHAIR_SEAT_Y)
                .position();
        assertEquals(0.5, small.x, EPSILON);
        assertEquals(0.5, large.x, EPSILON);
        assertEquals(TardisDecorShapes.SMALL_CHAIR_SEAT_Y, small.y, EPSILON);
        assertEquals(TardisDecorShapes.LARGE_CHAIR_SEAT_Y, large.y, EPSILON);
    }

    @Test
    void dismountCandidates_preferOpenSideThenSidesThenFacing() {
        assertEquals(
                List.of(Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH),
                TardisSeatPoses.dismountCandidateDirections(Direction.NORTH));
        assertEquals(
                List.of(Direction.WEST, Direction.SOUTH, Direction.NORTH, Direction.EAST),
                TardisSeatPoses.dismountCandidateDirections(Direction.EAST));
    }
}
