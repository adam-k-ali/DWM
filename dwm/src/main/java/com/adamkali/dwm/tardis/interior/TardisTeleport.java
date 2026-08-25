package com.adamkali.dwm.tardis.interior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class TardisTeleport {
    private TardisTeleport() {
    }

    public static void teleport(ServerPlayer player, ServerLevel destination, BlockPos feetPos, float yaw) {
        Vec3 pos = new Vec3(feetPos.getX() + 0.5, feetPos.getY(), feetPos.getZ() + 0.5);
        TeleportTransition target = new TeleportTransition(
                destination,
                pos,
                Vec3.ZERO,
                yaw,
                player.getXRot(),
                TeleportTransition.DO_NOTHING
        );
        player.teleport(target);
    }
}
