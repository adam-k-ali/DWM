package com.adamkali.dwm;

import com.adamkali.dwm.analytics.DWMStatistics;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.DWMWoodTypes;
import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.command.TardisCommands;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMDataComponents;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.network.ServerPayloadTypeRegistry;
import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

/**
 * Shared common/server initialization. Loader entrypoints must install
 * {@link com.adamkali.dwm.platform.DwmPlatform} via {@link DwmServices#set} before calling {@link #init()}.
 */
public final class DwmCommon {
    private static final Logger LOGGER = LogUtils.getLogger();

    private DwmCommon() {
    }

    public static void init() {
        LOGGER.info("Initializing Doctor Who Mod");
        DWMConfig.init();
        DWMStatistics.initialize();
        DWMWoodTypes.initialize();
        DWMBlocks.initialize();
        DWMEntityTypes.initialize();
        DWMDataComponents.initialize();
        DWMItems.initialize();
        DWMBlockEntities.initialize();
        DWMSounds.initialize();
        ServerPayloadTypeRegistry.initialize();
        PortalStreamSyncService.initialize();
        TardisTravelService.initialize();
        TardisCommands.initialize();
        DwmServices.get().registerServerStarted(server -> {
            TardisDataLoader.tardisSaveDirectory = server.getWorldPath(LevelResource.ROOT).resolve("tardis_data");
        });
        DwmServices.get().registerAfterSave((server, flush, force) -> {
            TardisDataLoader.save();
        });

        LOGGER.info("Doctor Who Mod initialized");
    }
}
