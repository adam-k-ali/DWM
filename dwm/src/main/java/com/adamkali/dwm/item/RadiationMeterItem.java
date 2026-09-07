package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.world.SkaroDimensions;
import com.adamkali.dwm.world.radiation.RadiationExposureLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * Handheld environmental radiation meter. Reports ambient location radiation (not suit-mitigated).
 */
public class RadiationMeterItem extends Item {
    public RadiationMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(
            @NonNull Level level,
            @NonNull Player player,
            @NonNull InteractionHand hand
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        int percent = meterPercent(serverLevel, player.blockPosition());
        player.sendOverlayMessage(Component.translatable("dwm.console.radiation", percent));
        return InteractionResult.SUCCESS;
    }

    static int meterPercent(ServerLevel level, BlockPos pos) {
        if (SkaroDimensions.isSkaroWorld(level)) {
            float ambient = RadiationExposureLogic.ambientForBiome(
                    level.getBiome(pos).unwrapKey().orElse(null)
            );
            return RadiationExposureLogic.meterPercent(ambient);
        }
        ExteriorEnvironmentReadout.Reading reading = ExteriorEnvironmentReadout.fromSample(
                ExteriorEnvironmentReadout.sampleFacts(level, pos)
        );
        if (reading.noSignal() || ExteriorEnvironmentReadout.isNoSignal(reading.radiation())) {
            return 0;
        }
        return RadiationExposureLogic.meterPercent(reading.radiation());
    }
}
