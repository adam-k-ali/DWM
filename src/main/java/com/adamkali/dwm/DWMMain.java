package com.adamkali.dwm;

import com.adamkali.dwm.analytics.DWMStatistics;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.sound.DWMSounds;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class DWMMain implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Doctor Who Mod");
        DWMStatistics.initialize();
        DWMItems.initialize();
        DWMBlocks.initialize();
        DWMBlockEntities.initialize();
        DWMSounds.initialize();
        LOGGER.info("Doctor Who Mod initialized");

        DWMVersion.checkVersion();
    }

}
