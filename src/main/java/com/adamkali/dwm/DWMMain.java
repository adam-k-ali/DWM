package com.adamkali.dwm;

import com.adamkali.dwm.analytics.DWMStatistics;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.DWMWoodTypes;
import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.network.ServerPayloadTypeRegistry;
import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class DWMMain implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Doctor Who Mod");
        DWMConfig.init();
        DWMStatistics.initialize();
        DWMWoodTypes.initialize();
        DWMBlocks.initialize();
        DWMEntityTypes.initialize();
        DWMItems.initialize();
        DWMBlockEntities.initialize();
        DWMSounds.initialize();
        ServerPayloadTypeRegistry.initialize();
        PortalStreamSyncService.initialize();
        TardisTravelService.initialize();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TardisDataLoader.tardisSaveDirectory = server.getWorldPath(LevelResource.ROOT).resolve("tardis_data");
        });
        ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> {
            TardisDataLoader.save();
        });

        LOGGER.info("Doctor Who Mod initialized");
    }

}
