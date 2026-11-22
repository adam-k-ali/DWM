package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.slf4j.Logger;

/** Runs {@link TardisPlotMigration} when the world loads and moves players saved inside a migrated legacy plot. */
public final class TardisPlotMigrationHooks {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TardisPlotMigrationHooks() {
    }

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.player));
    }

    /** Called from {@code SERVER_STARTED} once the TARDIS save directory is set, before the first tick. */
    public static TardisPlotMigration.Report onServerStarted(MinecraftServer server) {
        ServerLevel level = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        Path directory = TardisPlotMigration.directory();
        if (level == null || directory == null) {
            return TardisPlotMigration.Report.NOTHING;
        }
        TardisPlotMigration.Report report = TardisPlotMigration.run(
                new ServerMigrationWorld(server, level),
                new ServerMigrationWorld.FileSnapshotStore(directory, level));
        if (report != TardisPlotMigration.Report.NOTHING) {
            LOGGER.info("TARDIS interior plots migrated to 10000-block plots: {} interiors moved, {} empty (resumed from {})",
                    report.migrated(), report.emptyMarkedCurrent(), report.resumedFrom());
        }
        return report;
    }

    /**
     * Moves a player who was saved inside a migrated legacy window (in {@code dwm:tardis}) by that interior's delta,
     * along with a respawn point there. Each player is checked once, on their first join after the migration.
     */
    public static void onJoin(ServerPlayer player) {
        onJoin(player, TardisDimensions.TARDIS_WORLD_KEY);
    }

    /** {@link #onJoin(ServerPlayer)} with the interior dimension given (GameTests use their own level). */
    public static void onJoin(ServerPlayer player, ResourceKey<Level> interiorDimension) {
        if (!TardisPlotMigration.isPlayerPending(player.getUUID())) {
            return;
        }
        try {
            if (interiorDimension.equals(player.level().dimension())) {
                Vec3i delta = TardisPlotMigration.playerDelta(player.getUUID(), player.blockPosition());
                if (delta != null) {
                    player.stopRiding();
                    player.teleportTo((ServerLevel) player.level(),
                            player.getX() + delta.getX(), player.getY() + delta.getY(), player.getZ() + delta.getZ(),
                            Set.of(), player.getYRot(), player.getXRot(), false);
                    LOGGER.info("Moved {} into their TARDIS's new interior plot", player.getName().getString());
                }
            }
            ServerPlayer.RespawnConfig respawn = player.getRespawnConfig();
            if (respawn != null && interiorDimension.equals(respawn.respawnData().globalPos().dimension())) {
                BlockPos spawn = respawn.respawnData().globalPos().pos();
                Vec3i delta = TardisPlotMigration.playerDelta(player.getUUID(), spawn);
                if (delta != null) {
                    LevelData.RespawnData data = respawn.respawnData();
                    player.setRespawnPosition(new ServerPlayer.RespawnConfig(
                            new LevelData.RespawnData(
                                    GlobalPos.of(interiorDimension, spawn.offset(delta)), data.yaw(), data.pitch()),
                            respawn.forced()), false);
                }
            }
        } finally {
            TardisPlotMigration.playerHandled(player.getUUID());
        }
    }
}
