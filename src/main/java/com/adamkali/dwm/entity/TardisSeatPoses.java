package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Pure seat placement math for TARDIS chairs (safe for unit tests without Entity bootstrap).
 */
public final class TardisSeatPoses {
    private TardisSeatPoses() {
    }

    /**
     * World pose for a seat: block center XZ, cushion Y, yaw matching chair {@code FACING}.
     */
    public static SeatPose seatPose(BlockPos chairPos, Direction facing, double seatHeight) {
        Vec3 position = new Vec3(
                chairPos.getX() + 0.5,
                chairPos.getY() + seatHeight,
                chairPos.getZ() + 0.5);
        float yaw = Direction.getYRot(facing);
        return new SeatPose(position, yaw);
    }

    public record SeatPose(Vec3 position, float yaw) {
    }
}
