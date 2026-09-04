package com.adamkali.dwm.world.radiation;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

/**
 * Server-authoritative Skaro ambient radiation. Recomputes each cadence tick; stores no dose.
 */
public final class RadiationExposureService {
    private RadiationExposureService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(RadiationExposureService::onEndTick);
    }

    static void onEndTick(MinecraftServer server) {
        if (server.getTickCount() % RadiationExposureLogic.TICK_INTERVAL != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() instanceof ServerLevel level) {
                applyExposure(player, level);
            }
        }
    }

    /**
     * Applies one exposure check for a player in the given level. Used by GameTests.
     */
    public static void applyExposure(Player player, ServerLevel level) {
        if (!RadiationExposureLogic.isEligiblePlayer(player)) {
            return;
        }
        if (!RadiationExposureLogic.isExposedDimension(level)) {
            return;
        }

        Holder<Biome> biome = level.getBiome(player.blockPosition());
        float ambient = RadiationExposureLogic.ambientForBiome(biome.unwrapKey().orElse(null));
        float effective = RadiationExposureLogic.effectiveExposure(
                ambient,
                RadiationExposureLogic.countSuitPieces(player)
        );
        float damage = RadiationExposureLogic.damageAmount(effective);
        if (!RadiationExposureLogic.shouldApplyDamage(effective) || damage <= 0.0F) {
            return;
        }

        Holder.Reference<DamageType> type = level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(DWMDamageTypes.RADIATION);
        player.hurtServer(level, new DamageSource(type), damage);
    }
}
