package com.adamkali.dwm.tardis;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.RotationSegment;

/**
 * Maps {@code TardisBlock.FACING_ROTATION} to the chameleon shell's visual door direction.
 * <p>
 * Raw rotation 0 follows the skull/banner south convention, but shell BER transforms
 * ({@code X-180} then {@code Y(yaw - 180)}) leave the doors facing the opposite horizontal
 * direction. Exit teleport and SOTO look-out use this door direction so they match the shell.
 */
public final class TardisExteriorFacing {
    private TardisExteriorFacing() {
    }

    /**
     * World direction the exterior doors face (and the side a player should exit toward).
     */
    public static Direction doorDirection(int facingRotation) {
        return rotationToHorizontal(facingRotation).getOpposite();
    }

    /**
     * {@code FACING_ROTATION} segment whose {@link #doorDirection(int)} is {@code doorFacing}.
     * Non-horizontal inputs fall back to north-facing doors.
     */
    public static int facingRotationForDoor(Direction doorFacing) {
        Direction horizontal = doorFacing != null && doorFacing.getAxis().isHorizontal()
                ? doorFacing
                : Direction.NORTH;
        return RotationSegment.convertToSegment(horizontal.getOpposite().toYRot());
    }

    /**
     * Yaw for a player entering through the exterior door. Keeps their heading relative to the door:
     * walking straight in leaves them facing away from the interior door (south, yaw 0).
     */
    public static float enterYaw(float playerYaw, int facingRotation) {
        return Mth.wrapDegrees(playerYaw - RotationSegment.convertToDegrees(facingRotation));
    }

    /**
     * Yaw for a player leaving through the interior door (which faces south). Walking straight out
     * leaves them heading away from the shell, along {@link #doorDirection(int)}.
     */
    public static float exitYaw(float playerYaw, int facingRotation) {
        return Mth.wrapDegrees(playerYaw + RotationSegment.convertToDegrees(facingRotation));
    }

    private static Direction rotationToHorizontal(int rotation) {
        float yaw = RotationSegment.convertToDegrees(rotation);
        return Direction.fromYRot(yaw);
    }
}
