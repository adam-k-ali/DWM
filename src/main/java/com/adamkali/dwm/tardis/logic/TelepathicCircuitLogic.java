package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Arms {@link DestinationMode#TELEPATHIC} onto the using player's bed/respawn,
 * or that dimension's world spawn when no bed is set.
 */
public final class TelepathicCircuitLogic {
    private TelepathicCircuitLogic() {
    }

    public record Destination(String dimensionId, int x, int y, int z, boolean usedHome) {
    }

    /**
     * Pure: prefer an explicit bed/respawn, otherwise world spawn.
     */
    public static Destination resolve(
            @Nullable Destination bedOrRespawn,
            Destination worldSpawn
    ) {
        if (bedOrRespawn != null
                && bedOrRespawn.dimensionId() != null
                && !bedOrRespawn.dimensionId().isBlank()) {
            return new Destination(
                    bedOrRespawn.dimensionId(),
                    bedOrRespawn.x(),
                    bedOrRespawn.y(),
                    bedOrRespawn.z(),
                    true
            );
        }
        return new Destination(
                worldSpawn.dimensionId(),
                worldSpawn.x(),
                worldSpawn.y(),
                worldSpawn.z(),
                false
        );
    }

    public static Destination worldSpawnOf(ServerLevel world) {
        LevelData.RespawnData spawn = world.getRespawnData();
        BlockPos pos = spawn.pos();
        ResourceKey<Level> dimension = spawn.dimension();
        if (dimension == null) {
            dimension = world.dimension();
        }
        return new Destination(
                dimension.identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                false
        );
    }

    public static Optional<Destination> bedOrRespawnOf(@Nullable ServerPlayer player) {
        if (player == null) {
            return Optional.empty();
        }
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        if (config == null) {
            return Optional.empty();
        }
        LevelData.RespawnData data = config.respawnData();
        if (data == null || data.pos() == null) {
            return Optional.empty();
        }
        ResourceKey<Level> dimension = data.dimension();
        if (dimension == null) {
            dimension = player.level().dimension();
        }
        BlockPos pos = data.pos();
        return Optional.of(new Destination(
                dimension.identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                true
        ));
    }

    public static Destination resolveFor(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        return resolve(bedOrRespawnOf(player).orElse(null), worldSpawnOf(world));
    }

    /**
     * Arms telepathic mode onto {@code playerUuid}. Mode discriminates from
     * {@link DestinationMode#PLAYER} which reuses the same UUID field.
     */
    public static void arm(@Nullable TardisDataModel model, @Nullable UUID playerUuid) {
        if (model == null || playerUuid == null) {
            return;
        }
        model.selectedPlayerUuid = playerUuid;
        model.selectedWaypointId = null;
        model.selectedFastReturnIndex = 0;
        model.setDestinationMode(DestinationMode.TELEPATHIC);
    }

    public static boolean hasSelection(@Nullable TardisDataModel model) {
        return model != null
                && model.getDestinationMode() == DestinationMode.TELEPATHIC
                && model.selectedPlayerUuid != null;
    }
}
