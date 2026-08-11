package com.adamkali.dwm.tardis;

import net.minecraft.core.Direction;
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

    private static Direction rotationToHorizontal(int rotation) {
        float yaw = RotationSegment.convertToDegrees(rotation);
        return Direction.fromYRot(yaw);
    }
}
