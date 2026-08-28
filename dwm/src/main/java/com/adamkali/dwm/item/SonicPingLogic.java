package com.adamkali.dwm.item;

import com.adamkali.dwm.advancement.DWMCriteria;
import com.adamkali.dwm.network.SonicPingRevealS2CPayload;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.CircuitFittedLogic;
import com.adamkali.dwm.tardis.logic.CloakLogic;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pure Ping eligibility: cloak fitted + engaged, 32-block same-dimension range, 2s cooldown.
 */
public final class SonicPingLogic {
    public static final double RANGE_BLOCKS = 32.0;
    public static final int COOLDOWN_TICKS = 40;
    public static final int REVEAL_TICKS = 40;

    private static final Map<UUID, Long> LAST_SUCCESS_GAME_TIME = new ConcurrentHashMap<>();

    private SonicPingLogic() {
    }

    public enum Result {
        NOT_INSTALLED,
        CLOAK_NOT_FITTED,
        CLOAK_NOT_ENGAGED,
        NO_SIGNAL,
        ON_COOLDOWN,
        LOCATED
    }

    public static Result evaluate(
            boolean pingUnlocked,
            @Nullable TardisDataModel owned,
            @Nullable String playerDimensionId,
            double playerX,
            double playerY,
            double playerZ,
            long gameTime,
            long lastSuccessGameTime
    ) {
        if (!pingUnlocked) {
            return Result.NOT_INSTALLED;
        }
        if (owned == null || !owned.hasExteriorLocation || owned.exteriorDimension == null
                || owned.exteriorDimension.isBlank()) {
            return Result.NO_SIGNAL;
        }
        if (CircuitFittedLogic.isBroken(owned, TardisCircuit.CLOAK)) {
            return Result.CLOAK_NOT_FITTED;
        }
        if (!CloakLogic.isCloaked(owned)) {
            return Result.CLOAK_NOT_ENGAGED;
        }
        if (playerDimensionId == null || !playerDimensionId.equals(owned.exteriorDimension)) {
            return Result.NO_SIGNAL;
        }
        double dx = (owned.exteriorX + 0.5) - playerX;
        double dy = owned.exteriorY - playerY;
        double dz = (owned.exteriorZ + 0.5) - playerZ;
        if ((dx * dx) + (dy * dy) + (dz * dz) > RANGE_BLOCKS * RANGE_BLOCKS) {
            return Result.NO_SIGNAL;
        }
        if (lastSuccessGameTime != Long.MIN_VALUE
                && gameTime - lastSuccessGameTime < COOLDOWN_TICKS) {
            return Result.ON_COOLDOWN;
        }
        return Result.LOCATED;
    }

    public static Result tryPing(ServerPlayer player, ItemStack stack) {
        boolean unlocked = SonicStateLogic.isUnlocked(stack, SonicFieldMode.PING);
        TardisDataModel owned = TardisDataLoader.findOwnedBy(player.getUUID()).orElse(null);
        long gameTime = player.level().getGameTime();
        long lastSuccess = LAST_SUCCESS_GAME_TIME.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        Result result = evaluate(
                unlocked,
                owned,
                player.level().dimension().identifier().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                gameTime,
                lastSuccess
        );
        switch (result) {
            case NOT_INSTALLED -> player.sendOverlayMessage(Component.translatable(
                    SonicStateLogic.SETTING_NOT_INSTALLED_DETAIL_KEY,
                    Component.translatable(SonicFieldMode.PING.translationKey())
            ));
            case CLOAK_NOT_FITTED -> player.sendOverlayMessage(
                    Component.translatable(SonicStateLogic.PING_CLOAK_NOT_FITTED_KEY));
            case CLOAK_NOT_ENGAGED -> player.sendOverlayMessage(
                    Component.translatable(SonicStateLogic.PING_CLOAK_NOT_ENGAGED_KEY));
            case NO_SIGNAL -> player.sendOverlayMessage(
                    Component.translatable(SonicStateLogic.PING_NO_SIGNAL_KEY));
            case ON_COOLDOWN -> {
            }
            case LOCATED -> {
                LAST_SUCCESS_GAME_TIME.put(player.getUUID(), gameTime);
                player.sendOverlayMessage(Component.translatable(SonicStateLogic.PING_LOCATED_KEY));
                DWMCriteria.SONIC_PING.trigger(player);
                BlockPos pos = new BlockPos(owned.exteriorX, owned.exteriorY, owned.exteriorZ);
                ServerPlayNetworking.send(player, new SonicPingRevealS2CPayload(owned.uuid, pos));
            }
        }
        return result;
    }

    /** Test helper. */
    public static void clearCooldowns() {
        LAST_SUCCESS_GAME_TIME.clear();
    }
}
