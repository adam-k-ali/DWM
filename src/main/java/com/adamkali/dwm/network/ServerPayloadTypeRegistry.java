package com.adamkali.dwm.network;

import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class ServerPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(OpenTardisChameleonScreen.ID, OpenTardisChameleonScreen.CODEC);

        PayloadTypeRegistry.playC2S().register(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateTardisChameleonC2SPayload.ID, (payload, context) -> {
            Identifier variantId = payload.variantId();
            GlobalPos pos = payload.tardisPos();
            World world = context.server().getWorld(pos.dimension());
            if (world == null) {
                LOGGER.warn("Received UpdateTardisChameleonC2SPayload for invalid world: {}", pos.dimension());
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos.pos());

            if (!(blockEntity instanceof TardisBlockEntity tardis)) {
                LOGGER.warn("Received UpdateTardisChameleonC2SPayload for non-Tardis block entity: {}", blockEntity);
                return;
            }
            tardis.setVariant(TardisChameleonVariant.fromId(variantId));
            tardis.markDirty();
        });
    }
}
