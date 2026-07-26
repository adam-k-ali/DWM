package com.adamkali.dwm.tardis.interior;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public final class TardisTeleport {
    private TardisTeleport() {
    }

    public static void teleport(ServerPlayerEntity player, ServerWorld destination, BlockPos feetPos, float yaw) {
        Vec3d pos = new Vec3d(feetPos.getX() + 0.5, feetPos.getY(), feetPos.getZ() + 0.5);
        TeleportTarget target = new TeleportTarget(
                destination,
                pos,
                Vec3d.ZERO,
                yaw,
                player.getPitch(),
                TeleportTarget.NO_OP
        );
        player.teleportTo(target);
    }
}
